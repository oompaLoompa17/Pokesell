package com.example.Pokemon_TCG_TEST.RestController;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistration;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.Pokemon_TCG_TEST.Model.User;
import com.example.Pokemon_TCG_TEST.Repository.UserRepository;
import com.example.Pokemon_TCG_TEST.Service.MarketplaceService;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeTokenRequest;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;

import jakarta.servlet.http.HttpServletResponse;

@RestController
@CrossOrigin
@RequestMapping("/api/marketplace")
public class GoogleSheetsController {

    @Autowired private ClientRegistrationRepository clientRegistrationRepository;
    @Autowired private MarketplaceService marketplaceService;
    @Autowired private UserRepository userRepo;
    @Autowired private OAuth2AuthorizedClientService clientService;
    @Value("${spring.security.oauth2.client.registration.google.client-id}") private String clientId;
    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}") private String redirectUri;
    @Value("${spring.security.oauth2.client.registration.google.client-secret}") private String clientSecret;
    @Value("${app.frontend.url}") private String frontendUrl; // New property

    @GetMapping("/oauth2/authorize")
    public void authorize(HttpServletResponse response) throws IOException {
        String googleAuthUrl = "https://accounts.google.com/o/oauth2/v2/auth" +
            "?response_type=code" +
            "&client_id=" + clientId +
            "&scope=https://www.googleapis.com/auth/spreadsheets" +
            "&state=" + UUID.randomUUID().toString() +
            "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8);
            // "&redirect_uri=" + redirectUri;
        System.out.println("Redirecting to Google OAuth: " + googleAuthUrl);
        response.sendRedirect(googleAuthUrl);
    }

    @GetMapping("/check-auth")
    public ResponseEntity<Map<String, Boolean>> checkGoogleAuth(Authentication authentication) {
        boolean isAuthorized = false;
        if (authentication != null && authentication.isAuthenticated()) {
            OAuth2AuthorizedClient client = clientService.loadAuthorizedClient(
                "google", authentication.getName());
            isAuthorized = client != null && client.getAccessToken() != null;
        }
        return ResponseEntity.ok(Map.of("isAuthorized", isAuthorized));
    }
    
    @GetMapping("/export-sold")
    public void startExport(Authentication auth, HttpServletResponse response) throws IOException {
        if (auth == null || !auth.isAuthenticated()) {
            // response.sendRedirect("https://localhost:4300/marketplace/auctions?export=error&reason=login_required");
            response.sendRedirect(frontendUrl + "/marketplace/auctions?export=error&reason=login_required");
            return;
        }

        String email = auth.getName();
        User user = userRepo.findByEmail(email)
            .orElseThrow(() -> new IllegalStateException("User not found"));

        // Redirect to OAuth2 authorization if not yet authorized
        // Spring Security will handle this automatically when @RegisteredOAuth2AuthorizedClient is used
        // response.sendRedirect("https://localhost:8443/api/marketplace/export-sold-callback?userId=" + user.getId());
        response.sendRedirect("https://pokesell.org/api/marketplace/export-sold-callback?userId=" + user.getId());
    }

    @GetMapping("/export-sold-callback")
    public void exportSoldListings(
            @RequestParam("code") String code,
            @RequestParam("state") String state,
            Authentication auth,
            HttpServletResponse response) throws IOException {
        try {
            // ////////////////////////////////////////////////
            if (auth == null || !auth.isAuthenticated()) {
                // Redirect to login page if not authenticated
                String redirect = frontendUrl + "/login?redirect=/marketplace/auctions&reason=login_required";
                System.out.println("Redirecting (unauthenticated): " + redirect);
                response.sendRedirect(redirect);
                return;
            }
    
            String email = auth.getName();
            User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));
            // //////////////////////////////////////////////// 

            // Exchange the authorization code for an access token
            GoogleTokenResponse tokenResponse = new GoogleAuthorizationCodeTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                "https://oauth2.googleapis.com/token",
                clientId,
                clientSecret,
                code,
                // "https://localhost:8443/api/marketplace/export-sold-callback"
                redirectUri
            ).execute();

            String accessToken = tokenResponse.getAccessToken();
            // Retrieve the Google client registration
            ClientRegistration googleRegistration = clientRegistrationRepository.findByRegistrationId("google");
            if (googleRegistration == null) {
                throw new IllegalStateException("Google ClientRegistration not found!");
            }
    
            // Properly build OAuth2AuthorizedClient
            OAuth2AuthorizedClient client = new OAuth2AuthorizedClient(
                googleRegistration,  // Use the correct ClientRegistration
                auth.getName(),
                new org.springframework.security.oauth2.core.OAuth2AccessToken(
                    org.springframework.security.oauth2.core.OAuth2AccessToken.TokenType.BEARER,
                    accessToken,
                    null, // Issued at
                    null  // Expires at
                )
            );

            // // Find user
            // String email = auth.getName();
            // User user = userRepo.findByEmail(email)
            //     .orElseThrow(() -> new IllegalStateException("User not found"));
            Long userId = user.getId();

        //     // Export to Google Sheets
        //     String spreadsheetId = marketplaceService.exportSoldListingsToSheets(userId, client);
        //     if (spreadsheetId == null) {
        //         // response.sendRedirect("https://localhost:4300/marketplace/auctions?export=success&message=no_sold_listings");
        //         response.sendRedirect(frontendUrl + "/marketplace/auctions?export=success&message=no_sold_listings");
        //     } else {
        //         String spreadsheetUrl = "https://docs.google.com/spreadsheets/d/" + spreadsheetId;
        //         // response.sendRedirect("https://localhost:4300/marketplace/auctions?export=success&spreadsheetUrl=" + 
        //         response.sendRedirect(frontendUrl + "/marketplace/auctions?export=success&spreadsheetUrl=" + 
        //             URLEncoder.encode(spreadsheetUrl, StandardCharsets.UTF_8));
        //     }
        // } catch (Exception e) {
        //     // response.sendRedirect("https://localhost:4300/marketplace/auctions?export=error&reason=" + 
        //     response.sendRedirect(frontendUrl + "/marketplace/auctions?export=error&reason=" + 
        //         URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8));
        // }
            String spreadsheetId = marketplaceService.exportSoldListingsToSheets(userId, client);
            String redirectUrl;
            if (spreadsheetId == null) {
                redirectUrl = frontendUrl + "/marketplace/auctions?export=success&message=no_sold_listings";
            } else {
                String spreadsheetUrl = "https://docs.google.com/spreadsheets/d/" + spreadsheetId;
                redirectUrl = frontendUrl + "/marketplace/auctions?export=success&spreadsheetUrl=" + 
                    URLEncoder.encode(spreadsheetUrl, StandardCharsets.UTF_8);
            }
            System.out.println("Redirecting (success): " + redirectUrl);
            response.sendRedirect(redirectUrl);
        } catch (Exception e) {
            String redirectUrl = frontendUrl + "/marketplace/auctions?export=error&reason=" + 
                URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
            System.err.println("Redirecting (error): " + redirectUrl);
            response.sendRedirect(redirectUrl);
        }
    }
}