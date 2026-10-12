package com.transur.sigem.rrhh.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class DocumentoRequest {
    @NotNull(message = "El ID del empleado es obligatorio")
    private Long empleadoId;

    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipoDocumento; // LICENCIA_AIII_C, SCTR_SALUD, etc.

    private String numeroDocumento;

    @NotNull(message = "La fecha de emisión es obligatoria")
    private LocalDate fechaEmision;

    @NotNull(message = "La fecha de vencimiento es obligatoria")
    private LocalDate fechaVencimiento;

    private String rutaArchivo; // URL de Cloudinary tras subir imagen
}