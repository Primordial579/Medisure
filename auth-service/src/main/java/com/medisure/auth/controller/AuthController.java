package com.medisure.auth.controller;

import com.medisure.auth.dto.AuthResponse;
import com.medisure.auth.dto.ErrorResponse;
import com.medisure.auth.dto.LoginRequest;
import com.medisure.auth.dto.RegisterRequest;
import com.medisure.auth.service.AuthService;
import com.medisure.auth.service.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtService jwtService;

    public AuthController(AuthService authService, JwtService jwtService) {
        this.authService = authService;
        this.jwtService = jwtService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request) {
        try {
            AuthResponse response = authService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(400, "Bad Request", e.getMessage()));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            AuthResponse response = authService.login(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized", e.getMessage()));
        }
    }

    @GetMapping("/validate")
    public ResponseEntity<?> validateToken(@RequestHeader("Authorization") String authHeader) {
        try {
            if (authHeader == null || !authHeader.startsWith("Bearer ")) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(401, "Unauthorized", "Missing or invalid Authorization header"));
            }

            String token = authHeader.substring(7);

            if (!jwtService.validateToken(token)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(new ErrorResponse(401, "Unauthorized", "Invalid or expired token"));
            }

            String username = jwtService.getUsernameFromToken(token);
            String userType = jwtService.getUserTypeFromToken(token);
            String linkedUserId = jwtService.getLinkedUserIdFromToken(token);

            return ResponseEntity.ok(Map.of(
                    "valid", true,
                    "username", username,
                    "userType", userType,
                    "linkedUserId", linkedUserId != null ? linkedUserId : ""
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse(401, "Unauthorized", "Invalid or expired token"));
        }
    }
}
