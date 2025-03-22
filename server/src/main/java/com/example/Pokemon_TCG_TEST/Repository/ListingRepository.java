package com.example.Pokemon_TCG_TEST.Repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.Pokemon_TCG_TEST.Model.Listing;

public interface ListingRepository extends CrudRepository<Listing, Long> {
    List<Listing> findByUserIdAndStatus(Long userId, String status);
    List<Listing> findByStatus(String status); // For active listings
}