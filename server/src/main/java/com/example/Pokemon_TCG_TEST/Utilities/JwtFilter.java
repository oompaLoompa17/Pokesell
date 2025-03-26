package com.example.Pokemon_TCG_TEST.Utilities;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

    @Autowired
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        logger.info("Request URI: " + requestURI);

        // Skip filter for /api/auth/** and /api/marketplace/oauth2/**
        if (requestURI.startsWith("/api/auth/") || requestURI.startsWith("/api/marketplace/oauth2/")) {
            logger.info("Skipping JwtFilter for request: " + requestURI);
            chain.doFilter(request, response);
            return;
        }

        String authHeader = request.getHeader("Authorization");
        String token = null;
        String email = null;

        System.out.println("Request URL: " + request.getRequestURI());
        System.out.println("Authorization Header: " + authHeader);

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            token = authHeader.substring(7);
            System.out.println("Extracted Token: " + token);
            try {
                email = jwtUtil.extractEmail(token);
                System.out.println("Extracted Email: " + email);
            } catch (Exception e) {
                System.out.println("Token extraction failed: " + e.getMessage());
            }
        } else {
            System.out.println("No Bearer token found in header");
        }

        if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            if (jwtUtil.validateToken(token)) {
                System.out.println("Token validated successfully");
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(email, null, null);
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println("Token validation failed");
            }
        } else {
            System.out.println("Skipping authentication: email=" + email + ", existing auth=" + 
                SecurityContextHolder.getContext().getAuthentication());
        }

        chain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/api/auth/") || path.startsWith("/api/marketplace/oauth2/");
    }
}