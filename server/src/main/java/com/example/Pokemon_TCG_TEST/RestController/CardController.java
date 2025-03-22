package com.example.Pokemon_TCG_TEST.RestController;

import com.example.Pokemon_TCG_TEST.Model.Card;
import com.example.Pokemon_TCG_TEST.Service.CardService;
import com.example.Pokemon_TCG_TEST.Utilities.SetResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/cards")
@CrossOrigin(origins = "http://localhost:4200")
public class CardController {

    @Autowired
    private CardService cardService;

    @GetMapping("/filters")
    public ResponseEntity<?> getFilters() {
        List<SetResponse.SetDetails> sets = cardService.getAvailableSets();
        List<String> types = cardService.getAvailableTypes();
        List<String> rarities = cardService.getAvailableRarities();

        sets.sort(Comparator.comparing(SetResponse.SetDetails::getName));
        Collections.sort(types);
        Collections.sort(rarities);

        return ResponseEntity.ok(new FiltersResponse(sets, types, rarities));
    }
    //display all cards w same name
    //e.g. http://localhost:8080/api/search?name=alakazam will display all Alakazam cards in JSON
    @GetMapping("/search")
    public ResponseEntity<?> searchCards(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String set,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String rarity,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "12") int pageSize
    ) {
        List<Card> cards = cardService.searchCards(name, set, type, rarity, page, pageSize);
        int totalCards = cardService.getTotalCardCount(name, set, type, rarity);
        int totalPages = (int) Math.ceil((double) totalCards / pageSize);

        return ResponseEntity.ok(new CardSearchResponse(cards, page, totalPages));
    }
    
    //retrieves card by its unique ID
    //e.g. http://localhost:8080/api/base1-1 will show Alakazam from base set
    @GetMapping("/{id}")
    public ResponseEntity<?> getCardDetails(@PathVariable String id) {
        Card card = cardService.getCardById(id);
        return card != null ? ResponseEntity.ok(card) : ResponseEntity.notFound().build();
    }

    @GetMapping("/raw")
    public List<Card> getAllCards() {
        return cardService.getAllCards();
    }


    // Response classes
    public static class FiltersResponse {
        public List<SetResponse.SetDetails> sets;
        public List<String> types;
        public List<String> rarities;

        public FiltersResponse(List<SetResponse.SetDetails> sets, List<String> types, List<String> rarities) {
            this.sets = sets;
            this.types = types;
            this.rarities = rarities;
        }
    }

    public static class CardSearchResponse {
        public List<Card> cards;
        public int currentPage;
        public int totalPages;

        public CardSearchResponse(List<Card> cards, int currentPage, int totalPages) {
            this.cards = cards;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }
    }
}