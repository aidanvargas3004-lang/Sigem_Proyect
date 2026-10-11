package com.transmaqsur.sigem.dto;

import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoIncidencia;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO del formulario "Reportar incidencia". Solo expone lo que el usuario puede
 * ingresar; el servicio completa unidad, estado, código y quién reporta.
 */
@Getter
@Setter
public class IncidenciaForm {

    /** Asignación en curso (opcional). Si se indica, la unidad se toma de ella. */
    private Long asignacionId;

    private Long unidadId;

    @NotNull
    private TipoIncidencia tipo;

    @NotNull
    private Prioridad severidad = Prioridad.MEDIA;

    @NotNull
    private LocalDateTime fechaHora = LocalDateTime.now().withSecond(0).withNano(0);

    @NotBlank
    @Size(max = 1000)
    private String descripcion;

    @Size(max = 200)
    private String ubicacion;
}
