package com.transur.sigem.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UsuarioRegisterDTO {
    @NotBlank
    private String nombreCompleto;
    @Email
    @NotBlank private String email;
    @NotBlank @Size(min = 4) private String username;
    @NotBlank @Size(min = 8) private String password;
    private String rol; // Solo para ADMIN
}