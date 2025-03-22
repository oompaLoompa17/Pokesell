package com.example.Pokemon_TCG_TEST.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("listings")
public class Listing {
    @Id private Long id;
    private Long userId;
    private String cardId;
    private byte[] frontImage;
    private byte[] backImage;
    private Double overallGrade;
    private Double startingPrice;
    private Double buyoutPrice;
    private String listingType; // "FIXED" or "AUCTION"
    private LocalDateTime auctionStart;
    private LocalDateTime auctionEnd;
    private String status;
    
    public Listing() {
    }

    public Listing(Long id, Long userId, String cardId, byte[] frontImage, byte[] backImage, Double overallGrade,Double startingPrice,
             Double buyoutPrice, String listingType, LocalDateTime auctionStart, LocalDateTime auctionEnd, String status) {
        this.id = id;
        this.userId = userId;
        this.cardId = cardId;
        this.frontImage = frontImage;
        this.backImage = backImage;
        this.overallGrade = overallGrade;
        this.startingPrice = startingPrice;
        this.buyoutPrice = buyoutPrice;
        this.listingType = listingType;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.status = status;
    }

    // Getters, setters, constructor
    public Long getId() {return id;}
    public void setId(Long id) {this.id = id;}
    public Long getUserId() {return userId;}
    public void setUserId(Long userId) {this.userId = userId;}
    public String getCardId() {return cardId;}
    public void setCardId(String cardId) {this.cardId = cardId;}
    public byte[] getFrontImage() {return frontImage;}
    public void setFrontImage(byte[] frontImage) {this.frontImage = frontImage;}
    public byte[] getBackImage() {return backImage;}
    public void setBackImage(byte[] backImage) {this.backImage = backImage;}
    public Double getOverallGrade() {return overallGrade;}
    public void setOverallGrade(Double overallGrade) {this.overallGrade = overallGrade;}  
    public Double getStartingPrice() {return startingPrice;}
    public void setStartingPrice(Double startingPrice) {this.startingPrice = startingPrice;}
    public Double getBuyoutPrice() {return buyoutPrice;}
    public void setBuyoutPrice(Double buyoutPrice) {this.buyoutPrice = buyoutPrice;}
    public String getListingType() {return listingType;}
    public void setListingType(String listingType) {this.listingType = listingType;}
    public LocalDateTime getAuctionStart() {return auctionStart;}
    public void setAuctionStart(LocalDateTime auctionStart) {this.auctionStart = auctionStart;}
    public LocalDateTime getAuctionEnd() {return auctionEnd;}
    public void setAuctionEnd(LocalDateTime auctionEnd) {this.auctionEnd = auctionEnd;}
    public String getStatus() {return status;}
    public void setStatus(String status) {this.status = status;}
    
}