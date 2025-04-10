package com.example.Pokemon_TCG_TEST.Repository;

import java.util.List;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import com.example.Pokemon_TCG_TEST.Model.Listing;

public interface ListingRepository extends CrudRepository<Listing, Long> {
    @Query("SELECT id, card_name, card_set, card_number, overall_grade, sold_price, sold_date, listing_type " +
           "FROM listings WHERE user_id = :userId AND status = 'SOLD'")
    List<Listing> findByUserIdAndStatus(Long userId, String status);
    List<Listing> findByStatus(String status); // For active listings
    // New method to support a list of statuses
    @Query("SELECT l.* FROM listings l WHERE l.status IN (:statuses)")
    List<Listing> findByStatusIn(@Param("statuses") List<String> statuses);
    
}