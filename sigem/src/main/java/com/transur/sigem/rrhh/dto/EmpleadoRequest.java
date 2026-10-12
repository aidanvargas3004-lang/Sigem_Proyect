package com.transur.sigem.rrhh.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class EmpleadoRequest {
    @NotBlank(message = "El documento es obligatorio")
    private String documento;

    @NotBlank(message = "Los nombres son obligatorios")
    private String nombres;

    @NotBlank(message = "Los apellidos son obligatorios")
    private String apellidos;

    @NotBlank(message = "El cargo es obligatorio")
    private String cargo;

    private String telefono;

    @Email(message = "Formato de email inválido")
    private String email;

    @NotNull(message = "La fecha de ingreso es obligatoria")
    private LocalDate fechaIngreso;

    private Long usuarioId; // Opcional: vincular a usuario del sistema
}