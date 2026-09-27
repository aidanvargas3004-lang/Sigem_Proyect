package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.Modalidad;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.OneToOne;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Datos comunes a un alquiler de maquinaria y a un servicio de transporte:
 * cliente, contrato, unidad/personal asignado, tarifa y lecturas para calcular
 * las horas o kilómetros facturables.
 */
@Getter
@Setter
@MappedSuperclass
public abstract class Operacion extends EntidadBase {

    @Column(unique = true, length = 20)
    private String codigo;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "contrato_id")
    private Contrato contrato;

    @Valid
    @OneToOne(cascade = CascadeType.ALL, optional = false)
    @JoinColumn(name = "asignacion_id", unique = true)
    private Asignacion asignacion = new Asignacion();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;

    /** Si se deja vacía se toma la tarifa de la unidad según la modalidad. */
    @PositiveOrZero
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifa;

    @Column(precision = 12, scale = 1)
    private BigDecimal lecturaInicial;

    @Column(precision = 12, scale = 1)
    private BigDecimal lecturaFinal;

    private LocalDateTime inicioReal;

    private LocalDateTime finReal;

    /** Horas, días, km o viajes a facturar (calculado al finalizar). */
    @Column(precision = 12, scale = 2)
    private BigDecimal cantidadFacturable;

    @Column(precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOperacion estado = EstadoOperacion.PROGRAMADO;

    private boolean facturado;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observaciones;

    public Unidad getUnidad() {
        return asignacion != null ? asignacion.getUnidad() : null;
    }

    public Empleado getEmpleado() {
        return asignacion != null ? asignacion.getEmpleado() : null;
    }

    public boolean isFacturable() {
        return estado == EstadoOperacion.FINALIZADO && !facturado;
    }

    /** Texto usado en comprobantes y reportes. */
    public abstract String getConcepto();
}
