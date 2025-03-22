package com.example.Pokemon_TCG_TEST.Repository;

import java.util.Optional;

import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

import com.example.Pokemon_TCG_TEST.Model.Bid;

public interface BidRepository extends CrudRepository<Bid, Long> {
    @Query("SELECT * FROM bids WHERE listing_id = :listingId ORDER BY bid_amount DESC LIMIT 1")
    Optional<Bid> findHighestBidByListingId(Long listingId);
}