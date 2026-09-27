package com.transmaqsur.sigem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

/** Alquiler de maquinaria pesada o de una unidad por periodo. */
@Getter
@Setter
@Entity
@Table(name = "alquiler")
public class Alquiler extends Operacion {

    @Size(max = 200)
    @Column(length = 200)
    private String ubicacionObra;

    @Override
    public String getConcepto() {
        return "Alquiler " + getCodigo() + " - " + getUnidad().getTipo().getLabel() + " " + getUnidad().getCodigo()
                + (ubicacionObra != null ? " en " + ubicacionObra : "");
    }
}
