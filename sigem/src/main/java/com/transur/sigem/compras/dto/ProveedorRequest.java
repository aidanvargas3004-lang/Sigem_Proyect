package com.transur.sigem.compras.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ProveedorRequest {
    @NotBlank(message = "El tipo de proveedor es obligatorio")
    private String tipoProveedor; // NATURAL o JURIDICA

    @NotBlank(message = "La razón social es obligatoria")
    private String razonSocial;

    @NotBlank(message = "El documento (RUC/DNI) es obligatorio")
    private String documento;

    private String telefono;

    @Email(message = "Email inválido")
    private String email;

    private String direccion;
}