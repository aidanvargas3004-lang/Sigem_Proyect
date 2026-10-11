package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoIncidencia;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoIncidencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Incidencia ocurrida durante un servicio o alquiler (RF011 / CU006). */
@Getter
@Setter
@Entity
@Table(name = "incidencia")
public class Incidencia extends EntidadBase {

    @Column(unique = true, length = 20)
    private String codigo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    /** Asignación (alquiler, servicio u operación interna) durante la que ocurrió. */
    @ManyToOne
    @JoinColumn(name = "asignacion_id")
    private Asignacion asignacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoIncidencia tipo;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad severidad = Prioridad.MEDIA;

    @Column(nullable = false)
    private LocalDateTime fechaHora;

    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Column(length = 200)
    private String ubicacion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoIncidencia estado = EstadoIncidencia.REPORTADA;

    /** Conductor u operador que la reportó (si el usuario está vinculado a un empleado). */
    @ManyToOne
    @JoinColumn(name = "reportado_por_id")
    private Empleado reportadoPor;

    /** Orden correctiva generada para atenderla. */
    @ManyToOne
    @JoinColumn(name = "orden_mantenimiento_id")
    private OrdenMantenimiento ordenMantenimiento;

    @Column(length = 1000)
    private String solucion;

    private LocalDateTime fechaCierre;

    public boolean isAbierta() {
        return estado != EstadoIncidencia.RESUELTA;
    }
}
