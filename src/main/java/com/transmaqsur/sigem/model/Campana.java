package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.CanalMarketing;
import com.transmaqsur.sigem.model.enums.EstadoCampana;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
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
@Table(name = "campana")
public class Campana extends EntidadBase {

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CanalMarketing canal;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    @PositiveOrZero
    @Column(precision = 12, scale = 2)
    private BigDecimal presupuesto = BigDecimal.ZERO;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCampana estado = EstadoCampana.PLANIFICADA;

    @Size(max = 200)
    @Column(length = 200)
    private String publicoObjetivo;

    @Size(max = 1000)
    @Column(length = 1000)
    private String descripcion;
}
