package com.transur.sigem.compras.entity;

import com.transur.sigem.logistica.entity.Repuesto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "orden_compra_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrdenCompraDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repuesto_id")
    private Repuesto repuesto; // Nullable según tu DDL (para items no catalogados)

    @Column(nullable = false, length = 255)
    private String descripcion;

    @Column(nullable = false)
    private Integer cantidad; // Siempre > 0 (CHECK en BD)

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;
}