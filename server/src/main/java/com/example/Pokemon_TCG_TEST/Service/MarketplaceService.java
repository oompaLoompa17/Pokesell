package com.example.Pokemon_TCG_TEST.Service;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.stereotype.Service;

import com.example.Pokemon_TCG_TEST.Model.Bid;
import com.example.Pokemon_TCG_TEST.Model.Listing;
import com.example.Pokemon_TCG_TEST.Model.User;
import com.example.Pokemon_TCG_TEST.Repository.BidRepository;
import com.example.Pokemon_TCG_TEST.Repository.ListingRepository;
import com.example.Pokemon_TCG_TEST.Repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.model.ClearValuesRequest;
import com.google.api.services.sheets.v4.model.Spreadsheet;
import com.google.api.services.sheets.v4.model.SpreadsheetProperties;
import com.google.api.services.sheets.v4.model.ValueRange;
import com.google.auth.http.HttpCredentialsAdapter;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;

@Service
public class MarketplaceService {
    @Autowired private ListingRepository listingRepo;
    @Autowired private BidRepository bidRepo;
    @Autowired private UserRepository userRepo;
    @Autowired private TelegramBotService telegramBotService;
    @Autowired private OAuth2AuthorizedClientService clientService;

    public Listing createListing(Long userId, String cardName, String cardSet, String cardNumber, Map<String, Object> gradingResult,
                                 Double startingPrice, Double buyoutPrice, String listingType, LocalDateTime auctionStart,
                                 byte[] frontImage, byte[] backImage) throws JsonProcessingException {
        Listing listing = new Listing();
        listing.setUserId(userId);
        listing.setCardName(cardName);
        listing.setCardSet(cardSet);
        listing.setCardNumber(cardNumber);

        Map<String, Object> grades = (Map<String, Object>) gradingResult.get("grades");
        if (grades != null) {
            listing.setOverallGrade((Double) grades.get("final"));
        } else {
            throw new IllegalStateException("Top-level 'grades' missing in grading result");
        }

        listing.setFrontImage(frontImage);
        listing.setBackImage(backImage);
        listing.setStartingPrice(startingPrice);
        listing.setBuyoutPrice(buyoutPrice);
        listing.setListingType(listingType);
        if ("AUCTION".equals(listingType)) {
            listing.setAuctionStart(auctionStart);
            listing.setAuctionEnd(auctionStart.plusHours(1));
        }
        listing.setStatus("ACTIVE");
        return listingRepo.save(listing);
    }

    public List<Listing> getActiveListings() {
        List<Listing> listings = listingRepo.findByStatus("ACTIVE");
        listings.forEach(listing -> {
            if ("AUCTION".equals(listing.getListingType())) {
                Optional<Bid> highestBid = bidRepo.findHighestBidByListingId(listing.getId());
                listing.setStartingPrice(highestBid.map(Bid::getBidAmount).orElse(listing.getStartingPrice()));
            }
        });
        return listings;
    }

    public Bid placeBid(Long listingId, Long bidderId, Double bidAmount) {
        Listing listing = listingRepo.findById(listingId).orElseThrow();
        if (!"AUCTION".equals(listing.getListingType()) || LocalDateTime.now().isAfter(listing.getAuctionEnd())) {
            throw new IllegalStateException("Auction not active");
        }
        Bid bid = new Bid();
        bid.setListingId(listingId);
        bid.setBidderId(bidderId);
        bid.setBidAmount(bidAmount);
        return bidRepo.save(bid);
    }

    @Scheduled(fixedRate = 60000) // Check every minute
    public void closeAuctions() {
        LocalDateTime now = LocalDateTime.now();
        listingRepo.findByStatus("ACTIVE").stream()
            .filter(l -> "AUCTION".equals(l.getListingType()) && now.isAfter(l.getAuctionEnd()))
            .forEach(this::closeAuction);
    }

    public Listing getListingById(Long listingId) {
        return listingRepo.findById(listingId).orElseThrow(() -> new IllegalArgumentException("Listing not found"));
    }

    public void completePurchase(Long listingId, Long buyerId) {
        Listing listing = listingRepo.findById(listingId).orElseThrow(() -> new IllegalArgumentException("Listing not found"));
        if (!"ACTIVE".equals(listing.getStatus())) {
            throw new IllegalStateException("Listing is not available for purchase");
        }
        listing.setStatus("SOLD");
        listing.setSoldPrice(listing.getBuyoutPrice() != null ? listing.getBuyoutPrice() : listing.getStartingPrice());
        listing.setSoldDate(LocalDateTime.now()); 
        listingRepo.save(listing);
    }

    @Scheduled(fixedRate = 60000)
    public void checkAuctionEndTimes() {
        List<Listing> activeAuctions = listingRepo.findByStatus("ACTIVE");
        LocalDateTime now = LocalDateTime.now();

        for (Listing auction : activeAuctions) {
            if ("AUCTION".equals(auction.getListingType()) && auction.getAuctionEnd() != null) {
                long minutesLeft = ChronoUnit.MINUTES.between(now, auction.getAuctionEnd());
                if (minutesLeft <= 5 && minutesLeft > 0) {
                    if (!hasNotified(auction.getId())) {
                        String message = String.format(
                            "Auction #%d (Grade %s) is ending soon! Less than %d minute%s left. Current bid: SGD %.2f",
                            auction.getId(), auction.getOverallGrade(), minutesLeft, minutesLeft == 1 ? "" : "s",
                            auction.getStartingPrice()
                        );
                        telegramBotService.sendNotification(message);
                        markAsNotified(auction.getId());
                    }
                } else if (minutesLeft <= 0 && "ACTIVE".equals(auction.getStatus())) {
                    closeAuction(auction);
                }
            }
        }
    }

    private final Set<Long> notifiedAuctions = Collections.synchronizedSet(new HashSet<>());

    private boolean hasNotified(Long listingId) {
        return notifiedAuctions.contains(listingId);
    }

    private void markAsNotified(Long listingId) {
        notifiedAuctions.add(listingId);
    }

    private void closeAuction(Listing auction) {
        Optional<Bid> highestBid = bidRepo.findHighestBidByListingId(auction.getId());
        if (highestBid.isPresent()) {
            auction.setStatus("SOLD");
            auction.setSoldPrice(highestBid.get().getBidAmount()); // Set soldPrice to highest bid
            auction.setSoldDate(LocalDateTime.now());
        } else {
            auction.setStatus("EXPIRED");
        }
        listingRepo.save(auction);
        String message = String.format(
            "Auction #%d has ended! %s",
            auction.getId(),
            highestBid.map(bid -> "Sold for SGD " + String.format("%.2f", bid.getBidAmount())).orElse("No bids received.")
        );
        telegramBotService.sendNotification(message);
        notifiedAuctions.remove(auction.getId());
    }

    public List<Listing> getSoldListingsByUser(Long userId) {
        return listingRepo.findByUserIdAndStatus(userId, "SOLD");
    }

    public String exportSoldListingsToSheets(Long userId, OAuth2AuthorizedClient client) 
            throws IOException, GeneralSecurityException {
        List<Listing> soldListings = getSoldListingsByUser(userId);
        if (soldListings.isEmpty()) {
            return null;
        }

        User user = userRepo.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("User not found"));
        String spreadsheetId = user.getSpreadsheetId();
        Sheets sheetsService = getSheetsService(client);

        if (spreadsheetId == null) {
            Spreadsheet spreadsheet = new Spreadsheet()
                .setProperties(new SpreadsheetProperties()
                    .setTitle("Pokemon TCG Sold Listings - User " + userId));
            spreadsheet = sheetsService.spreadsheets().create(spreadsheet).execute();
            spreadsheetId = spreadsheet.getSpreadsheetId();
            user.setSpreadsheetId(spreadsheetId);
            userRepo.save(user);
        }

        String range = "Sheet1!A1";
        List<List<Object>> values = new ArrayList<>();
        values.add(Arrays.asList("ID", "Card Name", "Sold Price", "Sold Date"));
        for (Listing listing : soldListings) {
            values.add(Arrays.asList(
                listing.getId(),
                listing.getCardName(),
                listing.getSoldPrice(),
                listing.getSoldDate() != null ? listing.getSoldDate().toString() : ""
            ));
        }

        ValueRange body = new ValueRange().setValues(values);
        sheetsService.spreadsheets().values()
            .update(spreadsheetId, range, body)
            .setValueInputOption("RAW")
            .execute();

        return spreadsheetId;
    }

    private Sheets getSheetsService(OAuth2AuthorizedClient client) throws IOException, GeneralSecurityException {
        String accessToken = client.getAccessToken().getTokenValue();
        GoogleCredentials credentials = GoogleCredentials.create(new AccessToken(accessToken, null));
        HttpRequestInitializer requestInitializer = new HttpCredentialsAdapter(credentials);

        return new Sheets.Builder(
            GoogleNetHttpTransport.newTrustedTransport(),
            GsonFactory.getDefaultInstance(),
            requestInitializer
        ).setApplicationName("Pokemon TCG Marketplace").build();
    }

    public boolean isGoogleAuthorized(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        OAuth2AuthorizedClient client = clientService.loadAuthorizedClient("google", authentication.getName());
        return client != null && client.getAccessToken() != null;
    }
}