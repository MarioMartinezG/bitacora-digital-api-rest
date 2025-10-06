package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.AuthSuccessResponse;
import com.diginexa.bitacora.dtos.LoginRequest;
import com.diginexa.bitacora.dtos.RegisterRequest;
import com.diginexa.bitacora.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthSuccessResponse> login(@Valid @RequestBody LoginRequest loginRequest) {
        AuthSuccessResponse response = authService.login(loginRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/register")
    public ResponseEntity<AuthSuccessResponse> register(@Valid @RequestBody RegisterRequest registerRequest) {
        AuthSuccessResponse response = authService.register(registerRequest);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthSuccessResponse> refreshToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String refreshToken = authHeader.substring(7);
            AuthSuccessResponse response = authService.refreshToken(refreshToken);
            return ResponseEntity.ok(response);
        }
        throw new RuntimeException("Refresh token no proporcionado");
    }
}
