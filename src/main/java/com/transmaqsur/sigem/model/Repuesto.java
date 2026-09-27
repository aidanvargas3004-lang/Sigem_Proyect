package com.transmaqsur.sigem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "repuesto")
public class Repuesto extends EntidadBase {

    @NotBlank
    @Size(max = 30)
    @Column(nullable = false, unique = true, length = 30)
    private String codigo;

    @NotBlank
    @Size(max = 120)
    @Column(nullable = false, length = 120)
    private String nombre;

    @Size(max = 60)
    @Column(length = 60)
    private String categoria;

    /** UND, LT, KG, GL, JGO... */
    @NotBlank
    @Size(max = 10)
    @Column(nullable = false, length = 10)
    private String unidadMedida = "UND";

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario = BigDecimal.ZERO;

    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal stockMinimo = BigDecimal.ZERO;

    @Size(max = 500)
    @Column(length = 500)
    private String descripcion;

    private boolean activo = true;

    public String getDescripcionCompleta() {
        return codigo + " - " + nombre;
    }
}
