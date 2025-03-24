package com.example.Pokemon_TCG_TEST.RestController;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Pokemon_TCG_TEST.Model.Card;
import com.example.Pokemon_TCG_TEST.Service.CardService;
import com.example.Pokemon_TCG_TEST.Service.FavoriteService;

@RestController
@RequestMapping("/api/favorites")
@CrossOrigin(origins = "http://localhost:4200")
public class FavController {
    @Autowired
    private CardService cardService;
    @Autowired
    private FavoriteService favoriteService;

    @GetMapping
    public ResponseEntity<?> viewFavorites() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "You must be logged in to view favorites."));
        }
        String email = auth.getName();
        List<String> favoriteIds = favoriteService.getAllFavourites(email);
        List<Card> favoriteCards = cardService.getCardsByIds(favoriteIds);
        return ResponseEntity.ok(Map.of("data", favoriteCards));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addFavorite(@RequestBody Map<String, String> requestBody) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "You need to be logged in to add favorites."));
        }
        String email = auth.getName();
        String cardId = requestBody.get("cardId");
        if (cardId == null || cardId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Invalid card ID."));
        }
        boolean added = favoriteService.addFavourite(email, cardId);
        return added ? ResponseEntity.ok(Map.of("message", "Card added to favorites!"))
                     : ResponseEntity.status(HttpStatus.CONFLICT)
                             .body(Map.of("message", "Card is already in favorites."));
    }

    @PostMapping("/remove")
    public ResponseEntity<?> removeFavorite(@RequestBody Map<String, String> requestBody) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal().equals("anonymousUser")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(Map.of("message", "You need to be logged in to remove favorites."));
        }
        String email = auth.getName();
        String cardId = requestBody.get("cardId");
        if (cardId == null || cardId.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "Invalid card ID."));
        }
        favoriteService.removeFavourite(email, cardId);
        return ResponseEntity.ok(Map.of("message", "Card removed from favorites!"));
    }
}
