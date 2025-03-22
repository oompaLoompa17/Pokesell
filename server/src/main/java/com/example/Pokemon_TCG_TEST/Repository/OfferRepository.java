package com.example.Pokemon_TCG_TEST.Repository;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.example.Pokemon_TCG_TEST.Model.Offer;

public interface OfferRepository extends CrudRepository<Offer, Long> {
    List<Offer> findByListingIdAndStatus(Long listingId, String status);
}