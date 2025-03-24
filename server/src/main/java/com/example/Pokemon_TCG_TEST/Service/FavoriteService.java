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