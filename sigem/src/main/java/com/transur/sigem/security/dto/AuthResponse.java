package com.transur.sigem.security.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AuthResponse {
    private String token;
    private String tokenType;
    private Long userId;
    private String username;
    private String role;
    private String nombres;
    private String apellidos;
}