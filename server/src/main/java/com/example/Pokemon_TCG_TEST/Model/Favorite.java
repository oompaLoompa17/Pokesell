package com.example.Pokemon_TCG_TEST.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

@Table("favorites")
public class Favorite {
    @Id
    private Long id;
    private Long userId;
    private String cardId;

    // Constructors
    public Favorite() {}
    public Favorite(Long userId, String cardId) {
        this.userId = userId;
        this.cardId = cardId;
    }

    // Getters, setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public String getCardId() { return cardId; }
    public void setCardId(String cardId) { this.cardId = cardId; }
}

