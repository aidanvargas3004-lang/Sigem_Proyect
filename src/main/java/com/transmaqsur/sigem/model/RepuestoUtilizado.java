package com.transmaqsur.sigem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

/** Repuesto consumido en una orden de mantenimiento (descuenta stock del almacén). */
@Getter
@Setter
@Entity
@Table(name = "repuesto_utilizado")
public class RepuestoUtilizado extends EntidadBase {

    @ManyToOne(optional = false)
    @JoinColumn(name = "orden_id")
    private OrdenMantenimiento orden;

    @ManyToOne(optional = false)
    @JoinColumn(name = "repuesto_id")
    private Repuesto repuesto;

    @ManyToOne(optional = false)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
