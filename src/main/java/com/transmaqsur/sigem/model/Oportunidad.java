package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EtapaOportunidad;
import com.transmaqsur.sigem.model.enums.TipoServicio;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "oportunidad")
public class Oportunidad extends EntidadBase {

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String titulo;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "campana_id")
    private Campana campana;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EtapaOportunidad etapa = EtapaOportunidad.PROSPECTO;

    @PositiveOrZero
    @Column(precision = 12, scale = 2)
    private BigDecimal montoEstimado = BigDecimal.ZERO;

    @Min(0)
    @Max(100)
    private int probabilidad = 10;

    private LocalDate fechaCierreEstimada;

    @ManyToOne
    @JoinColumn(name = "responsable_id")
    private Usuario responsable;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descripcion;

    /** Solicitud generada a partir de esta oportunidad (si existe). */
    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    private Solicitud solicitud;

    public BigDecimal getMontoPonderado() {
        return montoEstimado == null ? BigDecimal.ZERO
                : montoEstimado.multiply(BigDecimal.valueOf(probabilidad)).divide(BigDecimal.valueOf(100));
    }
}
