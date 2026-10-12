package com.transur.sigem.security.controller;

import com.transur.sigem.security.dto.AuthResponse;
import com.transur.sigem.security.dto.LoginRequest;
import com.transur.sigem.security.dto.RegisterRequest;
import com.transur.sigem.security.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCurrentUser(@RequestHeader("Authorization") String authHeader) {
        // Aquí puedes extraer el token y devolver info del usuario actual
        // Por ahora retornamos un placeholder
        return ResponseEntity.ok("Endpoint /me funcional. Token recibido: " + authHeader.substring(7));
    }

}