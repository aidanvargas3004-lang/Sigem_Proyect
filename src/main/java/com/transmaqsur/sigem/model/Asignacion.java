package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoAsignacion;
import com.transmaqsur.sigem.model.enums.OrigenAsignacion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Reserva de una unidad (y opcionalmente de un conductor u operador) en un rango
 * de fechas. Es la pieza que evita la duplicidad de asignaciones.
 */
@Getter
@Setter
@Entity
@Table(name = "asignacion")
public class Asignacion extends EntidadBase {

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @ManyToOne
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrigenAsignacion origen = OrigenAsignacion.INTERNA;

    /** Código del documento que originó la asignación (ALQ-00001, SRV-00002...). */
    @Column(length = 20)
    private String referencia;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaInicio;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoAsignacion estado = EstadoAsignacion.PROGRAMADA;

    @Size(max = 300)
    @Column(length = 300)
    private String descripcion;

    public boolean isActiva() {
        return estado == EstadoAsignacion.PROGRAMADA || estado == EstadoAsignacion.EN_CURSO;
    }
}
