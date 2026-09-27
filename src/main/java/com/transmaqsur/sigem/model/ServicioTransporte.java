package com.transmaqsur.sigem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Servicio de transporte de carga pesada (operaciones). */
@Getter
@Setter
@Entity
@Table(name = "servicio_transporte")
public class ServicioTransporte extends Operacion {

    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    private Solicitud solicitud;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String origen;

    @NotBlank
    @Size(max = 200)
    @Column(nullable = false, length = 200)
    private String destino;

    @Size(max = 150)
    @Column(length = 150)
    private String tipoCarga;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal pesoToneladas;

    @Size(max = 30)
    @Column(length = 30)
    private String guiaRemision;

    @Override
    public String getConcepto() {
        return "Transporte " + getCodigo() + ": " + origen + " - " + destino
                + (tipoCarga != null ? " (" + tipoCarga + ")" : "");
    }
}
