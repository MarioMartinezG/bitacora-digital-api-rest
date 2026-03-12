package com.diginexa.bitacora.controllers;

import com.diginexa.bitacora.dtos.AuthSuccessResponse;
import com.diginexa.bitacora.dtos.CambiarClaveRequest;
import com.diginexa.bitacora.dtos.LoginRequest;
import com.diginexa.bitacora.dtos.MessageResponse;
import com.diginexa.bitacora.dtos.RecuperarClaveRequest;
import com.diginexa.bitacora.dtos.RegisterRequest;
import com.diginexa.bitacora.services.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
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

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout() {
        return ResponseEntity.ok(MessageResponse.of("Sesión cerrada exitosamente"));
    }

    @PostMapping("/recuperar-clave")
    public ResponseEntity<MessageResponse> recuperarClave(@Valid @RequestBody RecuperarClaveRequest request) {
        authService.recuperarClave(request);
        // Siempre responde OK para no revelar si el correo existe
        return ResponseEntity.ok(MessageResponse.of("Si el correo está registrado, recibirás una contraseña temporal"));
    }

    @PutMapping("/cambiar-clave")
    public ResponseEntity<MessageResponse> cambiarClave(
            @Valid @RequestBody CambiarClaveRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(401).body(MessageResponse.of("No autenticado"));
        }
        authService.cambiarClave(userDetails.getUsername(), request);
        return ResponseEntity.ok(MessageResponse.of("Contraseña actualizada correctamente"));
    }
}
