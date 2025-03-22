package com.example.Pokemon_TCG_TEST.RestController;

import com.example.Pokemon_TCG_TEST.Repository.UserRepository;
import com.example.Pokemon_TCG_TEST.Service.TelegramBotService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/telegram")
public class TelegramController {

    @Autowired private TelegramBotService telegramBotService;
    @Autowired private UserRepository userRepo;

    @GetMapping("/subscribe")
    public ResponseEntity<Map<String, String>> generateSubscriptionDeepLink(Authentication auth) {
        Long userId = userRepo.findByEmail(auth.getName()).orElseThrow().getId();
        String deepLink = telegramBotService.generateSubscriptionDeepLink(userId);
        Map<String, String> response = new HashMap<>();
        if (deepLink != null) {
            response.put("deepLink", deepLink);
        }
        response.put("subscribed", String.valueOf(telegramBotService.isUserSubscribed(userId)));
        return ResponseEntity.ok(response);
    }

    @PostMapping("/link")
    public ResponseEntity<String> linkChatId(@RequestBody Map<String, String> request) {
        String code = request.get("code");
        String chatId = request.get("chatId");
        telegramBotService.linkChatId(code, chatId);
        return ResponseEntity.ok("Chat ID linked successfully");
    }
}