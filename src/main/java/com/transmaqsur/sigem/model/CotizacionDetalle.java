package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
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
@Table(name = "cotizacion_detalle")
public class CotizacionDetalle extends EntidadBase {

    @ManyToOne(optional = false)
    @JoinColumn(name = "cotizacion_id")
    private Cotizacion cotizacion;

    @Column(nullable = false, length = 300)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoUnidad tipoUnidad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Modalidad modalidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal cantidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}
