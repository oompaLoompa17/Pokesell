package com.example.Pokemon_TCG_TEST.Service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.Pokemon_TCG_TEST.Model.Favorite;
import com.example.Pokemon_TCG_TEST.Model.User;
import com.example.Pokemon_TCG_TEST.Repository.FavoriteRepository;
import com.example.Pokemon_TCG_TEST.Repository.UserRepository;

@Service
public class FavoriteService {
    @Autowired
    private FavoriteRepository favoriteRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private CardService cardService;

    public boolean addFavourite(String email, String cardId) {
        if (favoriteRepository.existsByUserEmailAndCardId(email, cardId)) {
            return false;
        }
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        Favorite favorite = new Favorite(user.getId(), cardId);
        favoriteRepository.save(favorite);
        return true;
    }

    public List<String> getAllFavourites(String email) {
        return favoriteRepository.findCardIdsByUserEmail(email);
    }

    public void removeFavourite(String email, String cardId) {
        favoriteRepository.deleteByUserEmailAndCardId(email, cardId);
    }
}

// import com.example.Pokemon_TCG_TEST.Model.Card;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.beans.factory.annotation.Qualifier;
// import org.springframework.data.redis.core.RedisTemplate;
// import org.springframework.stereotype.Service;

// import java.util.ArrayList;
// import java.util.List;

// //redundant after creating UserService, so that it is saved to each individual user account

// @Service
// public class FavService {

//     @Autowired
//     @Qualifier("redis-template-card")
//     private RedisTemplate<String, Card> redisTemplate; // handles storing and retrieving Card objects from Redis

//     private static final String FAVORITES_KEY = "favorites";

//     // Add a card to favorites
//     public boolean addFavoriteCard(Card card) {
//         if (card == null || redisTemplate.opsForHash().hasKey(FAVORITES_KEY, card.getId())) {
//             return false;  // Already exists or card not found
//         }
//         redisTemplate.opsForHash().put(FAVORITES_KEY, card.getId(), card);
//         return true;
//     }

//     // Get all favorite cards
//     public List<Card> getFavoriteCards() {
//         List<Object> rawValues = redisTemplate.opsForHash().values(FAVORITES_KEY);
//         List<Card> favoriteCards = new ArrayList<>();
//         for (Object raw : rawValues) {
//             if (raw instanceof Card) {
//                 favoriteCards.add((Card) raw);
//             }
//         }
//         return favoriteCards;
//     }

//     // Remove a card from favorites
//     public boolean removeFavoriteCard(String id) {
//         if (!redisTemplate.opsForHash().hasKey(FAVORITES_KEY, id)) {
//             return false;  // Not found
//         }
//         redisTemplate.opsForHash().delete(FAVORITES_KEY, id);
//         return true;
//     }
// }