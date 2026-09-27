package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoContrato;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Getter
@Setter
@Entity
@Table(name = "contrato")
public class Contrato extends EntidadBase {

    /** Días de anticipación con que se alerta el vencimiento. */
    public static final int DIAS_ALERTA_VENCIMIENTO = 30;

    @Column(unique = true, length = 20)
    private String codigo;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    /** Contrato que fue renovado por este. */
    @ManyToOne
    @JoinColumn(name = "contrato_anterior_id")
    private Contrato contratoAnterior;

    @Size(max = 150)
    @Column(length = 150)
    private String objeto;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoContrato estado = EstadoContrato.VIGENTE;

    @Size(max = 2000)
    @Column(length = 2000)
    private String condiciones;

    public long getDiasRestantes() {
        return ChronoUnit.DAYS.between(LocalDate.now(), fechaFin);
    }

    public boolean isPorVencer() {
        return estado == EstadoContrato.VIGENTE && getDiasRestantes() <= DIAS_ALERTA_VENCIMIENTO;
    }

    public String getDescripcion() {
        return codigo + " - " + cliente.getRazonSocial();
    }
}
