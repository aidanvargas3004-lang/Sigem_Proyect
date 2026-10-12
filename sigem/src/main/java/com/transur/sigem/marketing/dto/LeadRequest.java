package com.transur.sigem.marketing.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LeadRequest {
    private Long campaniaId; // Opcional

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    private String empresa;
    private String telefono;

    @Email(message = "Email inválido")
    private String email;
    // ✅ CAMBIA ESTO: Usa Long para el ID del servicio seleccionado
    private Long servicioId;

    private String origen; // WEB, REFERIDO, LINKEDIN, TELEFONO
    private String estado = "NUEVO";
}