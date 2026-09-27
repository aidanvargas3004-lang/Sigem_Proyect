package com.transmaqsur.sigem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Stock de un repuesto en un almacén. Solo se modifica mediante movimientos de inventario. */
@Getter
@Setter
@Entity
@SinAuditoria
@Table(name = "inventario", uniqueConstraints = @UniqueConstraint(columnNames = {"almacen_id", "repuesto_id"}))
public class Inventario extends EntidadBase {

    @ManyToOne(optional = false)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "repuesto_id")
    private Repuesto repuesto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad = BigDecimal.ZERO;

    public boolean isBajoMinimo() {
        return cantidad.compareTo(repuesto.getStockMinimo()) <= 0;
    }
}
