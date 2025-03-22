package com.example.Pokemon_TCG_TEST.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("offers")
public class Offer {
    @Id private Long id;
    private Long listingId;
    private Long buyerId;
    private Double offerPrice;
    private String status;
    private LocalDateTime createdAt;

    // Getters, setters, constructor
    public Offer(){}

    public Offer(Long buyerId, LocalDateTime createdAt, Long id, Long listingId, Double offerPrice, String status) {
        this.buyerId = buyerId;
        this.createdAt = createdAt;
        this.id = id;
        this.listingId = listingId;
        this.offerPrice = offerPrice;
        this.status = status;
    }

    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Long getListingId() {return listingId;}
    public void setListingId(Long listingId) {this.listingId = listingId;}
    public Long getBuyerId() {return buyerId;}
    public void setBuyerId(Long buyerId) {this.buyerId = buyerId;}
    public Double getOfferPrice() {return offerPrice;}
    public void setOfferPrice(Double offerPrice) {this.offerPrice = offerPrice;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    public LocalDateTime getCreatedAt() {return createdAt;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    
}
