package com.example.Pokemon_TCG_TEST.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.Pokemon_TCG_TEST.Model.Bid;
import com.example.Pokemon_TCG_TEST.Model.Listing;
import com.example.Pokemon_TCG_TEST.Model.Offer;
import com.example.Pokemon_TCG_TEST.Repository.BidRepository;
import com.example.Pokemon_TCG_TEST.Repository.ListingRepository;
import com.example.Pokemon_TCG_TEST.Repository.OfferRepository;
import com.fasterxml.jackson.core.JsonProcessingException;

@Service
public class MarketplaceService {
    @Autowired private ListingRepository listingRepo;
    @Autowired private OfferRepository offerRepo;
    @Autowired private BidRepository bidRepo;
    @Autowired private TelegramBotService telegramBotService;

    public Listing createListing(Long userId, String cardId, Map<String, Object> gradingResult, Double startingPrice, Double buyoutPrice, 
        String listingType, LocalDateTime auctionStart, byte[] frontImage, byte[] backImage) throws JsonProcessingException {
        Listing listing = new Listing();
        listing.setUserId(userId);
        listing.setCardId(cardId);

        Map<String, Object> grades = (Map<String, Object>) gradingResult.get("grades");
        if (grades != null) {
            listing.setOverallGrade((Double) grades.get("final"));
        } else {
            throw new IllegalStateException("Top-level 'grades' missing in grading result");
        }

        listing.setFrontImage(frontImage); // Save front image
        listing.setBackImage(backImage);   // Save back image
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

    public Offer makeOffer(Long listingId, Long buyerId, Double offerPrice) {
        Listing listing = listingRepo.findById(listingId).orElseThrow();
        if (!"FIXED".equals(listing.getListingType())) throw new IllegalStateException("Not a fixed-price listing");
        Offer offer = new Offer();
        offer.setListingId(listingId);
        offer.setBuyerId(buyerId);
        offer.setOfferPrice(offerPrice);
        return offerRepo.save(offer);
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
            .forEach(l -> {
                Optional<Bid> highestBid = bidRepo.findHighestBidByListingId(l.getId());
                l.setStatus(highestBid.isPresent() ? "SOLD" : "EXPIRED");
                listingRepo.save(l);
            });
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
        listingRepo.save(listing);
        // Optionally, record the transaction in a separate table
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
                            auction.getId(), auction.getOverallGrade(), minutesLeft, minutesLeft == 1 ? "" : "s", auction.getStartingPrice()
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
        auction.setStatus(highestBid.isPresent() ? "SOLD" : "EXPIRED");
        listingRepo.save(auction);
        String message = String.format(
            "Auction #%d has ended! %s",
            auction.getId(),
            highestBid.map(bid -> "Sold for SGD " + String.format("%.2f", bid.getBidAmount())).orElse("No bids received.")
        );
        telegramBotService.sendNotification(message);
        notifiedAuctions.remove(auction.getId());
    }
}