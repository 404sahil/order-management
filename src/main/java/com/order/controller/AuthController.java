package com.order.controller;

import com.order.dto.LoginRequest;
import com.order.dto.LoginResponse;
import com.order.service.JwtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    // Simple in-memory user store for demo purposes
    // In production, this should be in a database
    private static final String ADMIN = "admin";
    private static final String ADMIN_PASSWORD = "admin@#$123";

    public AuthController(JwtService jwtService, PasswordEncoder passwordEncoder) {
        this.jwtService = jwtService;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticates a user and returns a JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        logger.info("POST /api/auth/login - Login attempt for user: {}", request.getUsername());
        
        // Validate credentials and assign role using Java 8 Optional pattern
        String role = determineRole(request.getUsername(), request.getPassword())
                .orElseThrow(() -> {
                    logger.warn("Invalid login attempt for user: {}", request.getUsername());
                    return new BadCredentialsException("Invalid credentials");
                });

        String token = jwtService.generateToken(request.getUsername(), role);
        logger.info("Login successful for user: {} with role: {}", request.getUsername(), role);
        return ResponseEntity.ok(new LoginResponse(token));
    }

    private java.util.Optional<String> determineRole(String username, String password) {
        if (ADMIN.equals(username) && passwordEncoder.matches("password", ADMIN_PASSWORD)) {
            return java.util.Optional.of("ADMIN");
        } else if (username != null && password != null) {
            // In production, check against database and assign role based on user data
            // For demo purposes, assign USER role to any valid credentials
            return java.util.Optional.of("USER");
        }
        return java.util.Optional.empty();
    }
}

