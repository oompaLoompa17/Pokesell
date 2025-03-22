package com.example.Pokemon_TCG_TEST.Repository;

import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.Pokemon_TCG_TEST.Model.Favorite;

import java.util.List;

// This interface defines a repository for the Favorite entity, managing user-card relationships
// CrudRepository<Favorite, Long> indicates Favorite objects with a Long primary key (id)
public interface FavoriteRepository extends CrudRepository<Favorite, Long> {
    
    // Fetches all card IDs favorited by a user, identified by their email
    // @Query uses a subquery to convert email to user_id, then selects card_id from favorites
    // Returns a List<String> of card IDs (e.g., "card123", "card456")
    @Query("SELECT card_id FROM favorites WHERE user_id = (SELECT id FROM users WHERE email = :email)")
    List<String> findCardIdsByUserEmail(String email);
    // Why: Used to display a user’s favorite cards in the FavoritesComponent

    // Checks if a specific card is already favorited by a user
    // @Query uses EXISTS with a subquery to verify the user_id and card_id pair exists
    // Returns true if the favorite exists, false otherwise
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE user_id = (SELECT id FROM users WHERE email = :email) AND card_id = :cardId)")
    boolean existsByUserEmailAndCardId(String email, String cardId);
    // Why: Prevents adding duplicate favorites in addFavourite()

    // Deletes a favorite entry for a user and card
    // @Modifying tells spring-data-jdbc this is a modifying operation (DELETE), not a query
    // @Query executes a DELETE statement with a subquery to match user_id by email
    // No return value (void) since it’s an action, not a query
    @Modifying // Add this to indicate a modifying operation
    @Query("DELETE FROM favorites WHERE user_id = (SELECT id FROM users WHERE email = :email) AND card_id = :cardId)")
    void deleteByUserEmailAndCardId(String email, String cardId);
    // Why: Removes a card from a user’s favorites when triggered from FavoritesComponent
}
