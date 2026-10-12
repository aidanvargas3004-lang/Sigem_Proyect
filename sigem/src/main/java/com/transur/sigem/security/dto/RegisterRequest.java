package com.transur.sigem.security.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RegisterRequest {

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String username;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;

    @Email(message = "Formato de correo inválido")
    @NotBlank(message = "El correo es obligatorio")
    private String email;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    // ✅ AGREGA ESTE CAMPO PARA LA API REST
    // Para registro web viene null/empty, para API debe venir explícito
    private String rolNombre;

    // Campos específicos para clientes externos (opcionales para API)
    private String tipoCliente;
    private String telefono;
    private String empresa;
    private String invitationCode;
}