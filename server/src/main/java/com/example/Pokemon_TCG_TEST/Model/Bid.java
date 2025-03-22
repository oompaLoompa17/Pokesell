package com.example.Pokemon_TCG_TEST.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("bids")
public class Bid {
    @Id private Long id;
    private Long listingId;
    private Long bidderId;
    private Double bidAmount;
    private LocalDateTime createdAt;

    // Getters, setters, constructor
    public Bid() {
        this.createdAt = LocalDateTime.now(); // Default to current time
    }

    public Bid(Double bidAmount, Long bidderId, LocalDateTime createdAt, Long id, Long listingId) {
        this.bidAmount = bidAmount;
        this.bidderId = bidderId;
        this.createdAt = createdAt != null ? createdAt : LocalDateTime.now();
        this.id = id;
        this.listingId = listingId;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Long getListingId() {return listingId;}
    public void setListingId(Long listingId) {this.listingId = listingId;}
    public Long getBidderId() {return bidderId;}
    public void setBidderId(Long bidderId) {this.bidderId = bidderId;}
    public Double getBidAmount() {return bidAmount;}
    public void setBidAmount(Double bidAmount) {this.bidAmount = bidAmount;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}
}
