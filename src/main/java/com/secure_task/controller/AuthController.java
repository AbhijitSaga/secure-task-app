package com.secure_task.controller;

import com.secure_task.dto.AuthResponse;
import com.secure_task.dto.LoginRequest;
import com.secure_task.dto.RegisterRequest;
import com.secure_task.entity.User;
import com.secure_task.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    // ──────────────────────────────────────────────────
    // POST /api/auth/register
    // Body: { "name": "Raj", "email": "raj@gmail.com",
    //         "password": "pass1234" }
    // ──────────────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(
            @Valid @RequestBody RegisterRequest request) {

        AuthResponse response = authService.register(request);
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────
    // POST /api/auth/login
    // Body: { "email": "raj@gmail.com", "password": "pass1234" }
    // Returns: JWT token
    // ──────────────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {



        AuthResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    // ──────────────────────────────────────────────────
    // GET /api/auth/me
    // Header: Authorization: Bearer <token>
    // Returns: currently logged-in user's info
    // ──────────────────────────────────────────────────
    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(
            @AuthenticationPrincipal User user) {
        // @AuthenticationPrincipal automatically gives us the logged-in user!
        return ResponseEntity.ok(Map.of(
                "id",    user.getId(),
                "name",  user.getName(),
                "email", user.getEmail(),
                "role",  user.getRole(),
                "provider", user.getAuthProvider()
        ));
    }
}

