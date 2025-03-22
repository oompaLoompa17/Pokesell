package com.example.Pokemon_TCG_TEST.RestController;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.Pokemon_TCG_TEST.Model.Bid;
import com.example.Pokemon_TCG_TEST.Model.Listing;
import com.example.Pokemon_TCG_TEST.Repository.UserRepository;
import com.example.Pokemon_TCG_TEST.Service.CardGraderService;
import com.example.Pokemon_TCG_TEST.Service.MarketplaceService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.stripe.Stripe;
import com.stripe.model.PaymentIntent;

@RestController
@RequestMapping("/api/marketplace")
@CrossOrigin(origins = "http://localhost:4200")
public class MarketplaceController {

    @Value("${stripe.secret.key}") // Add to application.properties
    private String stripeSecretKey;
    
    @Autowired private UserRepository userRepo;
    @Autowired private MarketplaceService marketplaceService;
    @Autowired private CardGraderService graderService;

    @PostMapping("/list")
    public ResponseEntity<?> createListing(
        @RequestParam("frontImage") MultipartFile frontImage,
        @RequestParam("backImage") MultipartFile backImage,
        @RequestParam(value = "startingPrice", required = false) Double startingPrice,
        @RequestParam(value = "buyoutPrice", required = false) Double buyoutPrice,
        @RequestParam("listingType") String listingType,
        @RequestParam(value = "auctionStart", required = false) LocalDateTime auctionStart,
        Authentication auth
    ) throws JsonProcessingException, IOException {
        ResponseEntity<?> gradingResponse = graderService.gradeCard(frontImage, backImage);
        if (gradingResponse.getStatusCode() != HttpStatus.OK) {
            return gradingResponse;
        }
        Map<String, Object> gradingResult = (Map<String, Object>) gradingResponse.getBody();
        if (gradingResult == null) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("message", "Grading result is null"));
        }
        Long userId = userRepo.findByEmail(auth.getName()).orElseThrow().getId();
        Listing listing = marketplaceService.createListing(
            userId, "cardId", gradingResult, startingPrice, buyoutPrice, listingType, auctionStart,
            frontImage.getBytes(), backImage.getBytes() // Convert MultipartFile to byte[]
        );
        return ResponseEntity.ok(Map.of("message", "Listing created", "listingId", listing.getId()));
    }

    @GetMapping("/active")
    public ResponseEntity<List<Map<String, Object>>> getActiveListings() {
        List<Listing> activeListings = marketplaceService.getActiveListings();
        List<Map<String, Object>> responseList = activeListings.stream().map(listing -> {
            Map<String, Object> map = new HashMap<>();
            map.put("id", listing.getId());
            map.put("userId", listing.getUserId());
            map.put("cardId", listing.getCardId());
            map.put("overallGrade", listing.getOverallGrade());
            map.put("startingPrice", listing.getStartingPrice());
            map.put("buyoutPrice", listing.getBuyoutPrice());
            map.put("listingType", listing.getListingType());
            map.put("auctionStart", listing.getAuctionStart());
            map.put("auctionEnd", listing.getAuctionEnd());
            map.put("status", listing.getStatus());
            map.put("frontImage", Base64.getEncoder().encodeToString(listing.getFrontImage()));
            map.put("backImage", Base64.getEncoder().encodeToString(listing.getBackImage()));
            return map;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(responseList);
    }

    @PostMapping("/payment-intent")
    public ResponseEntity<Map<String, String>> createPaymentIntent(@RequestBody Map<String, Object> request) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Long listingId = Long.valueOf(request.get("listingId").toString());
            Double amount = Double.valueOf(request.get("amount").toString()) * 100; // Convert to cents

            Listing listing = marketplaceService.getListingById(listingId);
            if (!"ACTIVE".equals(listing.getStatus()) || !"FIXED".equals(listing.getListingType())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of("error", "Listing not available"));
            }

            PaymentIntent paymentIntent = PaymentIntent.create(
                new HashMap<String, Object>() {{
                    put("amount", amount.longValue());
                    put("currency", "sgd");
                    put("metadata", Map.of("listingId", listingId.toString()));
                }}
            );

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/purchase")
    public ResponseEntity<Map<String, String>> completePurchase(@RequestBody Map<String, Long> request, Authentication auth) {
        try {
            Long listingId = request.get("listingId");
            Long buyerId = userRepo.findByEmail(auth.getName()).orElseThrow().getId();
            marketplaceService.completePurchase(listingId, buyerId);
            return ResponseEntity.ok(Map.of("message", "Purchase completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/bid")
    public ResponseEntity<Map<String, Object>> placeBid(
        @RequestBody Map<String, Object> request,
        Authentication auth
    ) {
        try {
            Long listingId = Long.valueOf(request.get("listingId").toString());
            Double bidAmount = Double.valueOf(request.get("bidAmount").toString());
            Long bidderId = userRepo.findByEmail(auth.getName()).orElseThrow().getId();
            Bid bid = marketplaceService.placeBid(listingId, bidderId, bidAmount);
            return ResponseEntity.ok(Map.of(
                "message", "Bid placed successfully",
                "bidId", bid.getId(),
                "bidAmount", bid.getBidAmount()
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auction/buyout-intent")
    public ResponseEntity<Map<String, String>> createAuctionBuyoutIntent(
        @RequestBody Map<String, Object> request
    ) {
        try {
            Stripe.apiKey = stripeSecretKey;
            Long listingId = Long.valueOf(request.get("listingId").toString());
            Listing listing = marketplaceService.getListingById(listingId);
            if (!"ACTIVE".equals(listing.getStatus()) || !"AUCTION".equals(listing.getListingType()) || listing.getBuyoutPrice() == null) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "Buyout not available for this listing"));
            }

            Double amount = listing.getBuyoutPrice() * 100; // Convert to cents
            PaymentIntent paymentIntent = PaymentIntent.create(
                new HashMap<String, Object>() {{
                    put("amount", amount.longValue());
                    put("currency", "sgd");
                    put("metadata", Map.of("listingId", listingId.toString()));
                }}
            );

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", paymentIntent.getClientSecret());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/auction/buyout")
    public ResponseEntity<Map<String, String>> completeAuctionBuyout(
        @RequestBody Map<String, Long> request,
        Authentication auth
    ) {
        try {
            Long listingId = request.get("listingId");
            Long buyerId = userRepo.findByEmail(auth.getName()).orElseThrow().getId();
            marketplaceService.completePurchase(listingId, buyerId);
            return ResponseEntity.ok(Map.of("message", "Buyout completed successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", e.getMessage()));
        }
    }
}