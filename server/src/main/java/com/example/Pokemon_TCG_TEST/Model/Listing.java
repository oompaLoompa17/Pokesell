package com.example.Pokemon_TCG_TEST.Model;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("listings")
public class Listing {
    @Id private Long id;
    private Long userId;
    private String cardName;
    private String cardSet;
    private String cardNumber;
    private byte[] frontImage;
    private byte[] backImage;
    private Double overallGrade;
    private Double startingPrice;
    private Double buyoutPrice;
    private Double soldPrice;
    private LocalDateTime soldDate;
    private String listingType;
    private LocalDateTime auctionStart;
    private LocalDateTime auctionEnd;
    private String status;

    public Listing() {}

    public Listing(Long id, Long userId, String cardName, String cardSet, String cardNumber, byte[] frontImage, byte[] backImage,
                   Double overallGrade, Double startingPrice, Double buyoutPrice, Double soldPrice, LocalDateTime soldDate,
                   String listingType, LocalDateTime auctionStart, LocalDateTime auctionEnd, String status) {
        this.id = id;
        this.userId = userId;
        this.cardName = cardName;
        this.cardSet = cardSet;
        this.cardNumber = cardNumber;
        this.frontImage = frontImage;
        this.backImage = backImage;
        this.overallGrade = overallGrade;
        this.startingPrice = startingPrice;
        this.buyoutPrice = buyoutPrice;
        this.soldPrice = soldPrice;
        this.soldDate = soldDate;
        this.listingType = listingType;
        this.auctionStart = auctionStart;
        this.auctionEnd = auctionEnd;
        this.status = status;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCardName() { return cardName; }
    public void setCardName(String cardName) { this.cardName = cardName; }
    public String getCardSet() { return cardSet; }
    public void setCardSet(String cardSet) { this.cardSet = cardSet; }
    public String getCardNumber() { return cardNumber; }
    public void setCardNumber(String cardNumber) { this.cardNumber = cardNumber; }
    public byte[] getFrontImage() { return frontImage; }
    public void setFrontImage(byte[] frontImage) { this.frontImage = frontImage; }
    public byte[] getBackImage() { return backImage; }
    public void setBackImage(byte[] backImage) { this.backImage = backImage; }
    public Double getOverallGrade() { return overallGrade; }
    public void setOverallGrade(Double overallGrade) { this.overallGrade = overallGrade; }
    public Double getStartingPrice() { return startingPrice; }
    public void setStartingPrice(Double startingPrice) { this.startingPrice = startingPrice; }
    public Double getBuyoutPrice() { return buyoutPrice; }
    public void setBuyoutPrice(Double buyoutPrice) { this.buyoutPrice = buyoutPrice; }
    public Double getSoldPrice() { return soldPrice; }
    public void setSoldPrice(Double soldPrice) { this.soldPrice = soldPrice; }
    public LocalDateTime getSoldDate() { return soldDate; }
    public void setSoldDate(LocalDateTime soldDate) { this.soldDate = soldDate; }
    public String getListingType() { return listingType; }
    public void setListingType(String listingType) { this.listingType = listingType; }
    public LocalDateTime getAuctionStart() { return auctionStart; }
    public void setAuctionStart(LocalDateTime auctionStart) { this.auctionStart = auctionStart; }
    public LocalDateTime getAuctionEnd() { return auctionEnd; }
    public void setAuctionEnd(LocalDateTime auctionEnd) { this.auctionEnd = auctionEnd; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}