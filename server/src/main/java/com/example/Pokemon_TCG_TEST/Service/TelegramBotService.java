package com.example.Pokemon_TCG_TEST.Service;

import com.example.Pokemon_TCG_TEST.Model.User;
import com.example.Pokemon_TCG_TEST.Repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.UUID;

@Service
public class TelegramBotService {

    @Value("${telegram.bot.token}")
    private String botToken;

    @Value("${telegram.bot.username}")
    private String botUsername; // e.g., @PokemonAuctionBot

    @Autowired
    private UserRepository userRepo;

    private final RestTemplate restTemplate = new RestTemplate();

    public String generateSubscriptionDeepLink(Long userId) {
        User user = userRepo.findById(userId).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (user.getTelegramChatId() != null) {
            return null; // Already subscribed, no deep link needed
        }
        String code = UUID.randomUUID().toString();
        user.setTelegramSubscriptionCode(code);
        userRepo.save(user);
        return String.format("https://t.me/%s?start=%s", botUsername.substring(1), code);
    }

    public void linkChatId(String subscriptionCode, String chatId) {
        userRepo.findByTelegramSubscriptionCode(subscriptionCode).ifPresent(user -> {
            user.setTelegramChatId(chatId);
            userRepo.save(user);
            sendMessage(chatId, "Successfully linked your Telegram account to Pokémon TCG Marketplace!");
        });
    }

    public void sendNotification(String message) {
        List<User> users = (List<User>) userRepo.findAll();
        for (User user : users) {
            if (user.getTelegramChatId() != null) {
                sendMessage(user.getTelegramChatId(), message);
            }
        }
    }

    private void sendMessage(String chatId, String text) {
        String url = String.format("https://api.telegram.org/bot%s/sendMessage", botToken);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        String body = String.format("{\"chat_id\":\"%s\",\"text\":\"%s\"}", chatId, text);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            restTemplate.exchange(url, HttpMethod.POST, request, String.class);
        } catch (Exception e) {
            e.printStackTrace(); // Log errors in production
        }
    }

    public boolean isUserSubscribed(Long userId) {
        return userRepo.findById(userId)
            .map(user -> user.getTelegramChatId() != null)
            .orElse(false);
    }
}