package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.TipoMovimiento;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** Kardex: cada entrada o salida de stock queda registrada. */
@Getter
@Setter
@Entity
@SinAuditoria
@Table(name = "movimiento_inventario")
public class MovimientoInventario extends EntidadBase {

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMovimiento tipo;

    @ManyToOne(optional = false)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @ManyToOne(optional = false)
    @JoinColumn(name = "repuesto_id")
    private Repuesto repuesto;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal stockResultante;

    /** Documento que origina el movimiento: OC-00001, OT-00003, F001-00000012... */
    @Column(length = 40)
    private String referencia;

    @Column(length = 300)
    private String observacion;
}
