package com.transur.sigem.comercial.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "venta_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venta_id", nullable = false)
    private Venta venta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;

    @Column(name = "precio_final", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioFinal; // Precio real de transferencia (negociado)

    @Column(nullable = false)
    private Integer cantidad = 1; // Siempre > 0 (CHECK en BD)

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}