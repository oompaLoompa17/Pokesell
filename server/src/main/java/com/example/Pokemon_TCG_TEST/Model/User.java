package com.example.Pokemon_TCG_TEST.Model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import jakarta.validation.constraints.Pattern;

@Table("users")
public class User {
    @Id
    private Long id;
    private String email;
    @Pattern(regexp = "^(?=.*[!@#$%^&*()_+=|<>?{}\\[\\]~-]).*$", 
             message = "Password must contain at least one special character.")
    private String password;
    private String telegramChatId;
    private String telegramSubscriptionCode;
    private String spreadsheetId; // New field

    // Constructors
    public User() {}
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getTelegramChatId() { return telegramChatId; }
    public void setTelegramChatId(String telegramChatId) { this.telegramChatId = telegramChatId; }
    public String getTelegramSubscriptionCode() { return telegramSubscriptionCode; }
    public void setTelegramSubscriptionCode(String telegramSubscriptionCode) { this.telegramSubscriptionCode = telegramSubscriptionCode; }
    public String getSpreadsheetId() { return spreadsheetId; }
    public void setSpreadsheetId(String spreadsheetId) { this.spreadsheetId = spreadsheetId; }
}