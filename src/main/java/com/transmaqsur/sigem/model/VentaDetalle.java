package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.TipoLineaVenta;
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

@Getter
@Setter
@Entity
@Table(name = "venta_detalle")
public class VentaDetalle extends EntidadBase {

    @ManyToOne(optional = false)
    @JoinColumn(name = "venta_id")
    private Venta venta;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoLineaVenta tipoLinea;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(nullable = false, length = 10)
    private String unidadMedida = "UND";

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @ManyToOne
    @JoinColumn(name = "alquiler_id")
    private Alquiler alquiler;

    @ManyToOne
    @JoinColumn(name = "servicio_id")
    private ServicioTransporte servicio;

    @ManyToOne
    @JoinColumn(name = "repuesto_id")
    private Repuesto repuesto;

    @ManyToOne
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;
}
