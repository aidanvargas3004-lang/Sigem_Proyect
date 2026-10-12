package com.transur.sigem.cliente.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ClienteRequest {
    // Datos del Cliente
    @NotBlank private String documento;
    @NotBlank private String razonSocial;
    private String tipoCliente;
    private String direccion;
    private String telefono;
    private String email;

    // Datos de Usuario (Para crear y editar)
    private String username;      // Se pre-llena al editar, obligatorio al crear
    private String newPassword;   // Contraseña nueva (obligatoria al crear, opcional al editar)
    private String confirmPassword; // Confirmación (obligatoria al crear, opcional al editar)
}