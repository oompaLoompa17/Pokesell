package com.example.Pokemon_TCG_TEST.Utilities;

import org.springframework.web.client.RestTemplate;

public class TelegramUpdateHandler {

    private static final String BOT_TOKEN = "1234567890:AAFxxxxxxxxxxxxxxxxxxxxxxxxxxxx"; // Replace with your token
    private static final String BACKEND_URL = "http://localhost:8443/api/telegram/link"; // Adjust to your backend URL
    private static final RestTemplate restTemplate = new RestTemplate();
    private static long lastUpdateId = 0;

    public static void main(String[] args) {
        while (true) {
            try {
                pollUpdates();
                Thread.sleep(1000); // Poll every second
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Thread.sleep(5000); // Wait longer if there’s an error
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }
    }

    private static void pollUpdates() {
        String url = String.format("https://api.telegram.org/bot%s/getUpdates?offset=%d&timeout=60", BOT_TOKEN, lastUpdateId + 1);
        String response = restTemplate.getForObject(url, String.class);
        if (response != null && response.contains("\"ok\":true")) {
            // Parse JSON manually or use a library like Jackson
            // For simplicity, we’ll assume a basic parsing approach
            if (response.contains("\"message\"")) {
                String[] updates = response.split("\"update_id\":");
                for (String update : updates) {
                    if (update.contains("\"message\"")) {
                        long updateId = Long.parseLong(update.split(",")[0].trim());
                        lastUpdateId = Math.max(lastUpdateId, updateId);

                        String text = extractText(update);
                        String chatId = extractChatId(update);
                        if (text != null && text.startsWith("/start ") && chatId != null) {
                            String subscriptionCode = text.substring("/start ".length()).trim();
                            restTemplate.postForObject(BACKEND_URL, java.util.Map.of("code", subscriptionCode, "chatId", chatId), String.class);
                        }
                    }
                }
            }
        }
    }

    private static String extractText(String update) {
        int textIndex = update.indexOf("\"text\":\"");
        if (textIndex == -1) return null;
        int start = textIndex + 8;
        int end = update.indexOf("\"", start);
        return update.substring(start, end);
    }

    private static String extractChatId(String update) {
        int chatIdIndex = update.indexOf("\"chat\":{\"id\":");
        if (chatIdIndex == -1) return null;
        int start = chatIdIndex + 13;
        int end = update.indexOf(",", start);
        if (end == -1) end = update.indexOf("}", start);
        return update.substring(start, end);
    }
}