package com.transur.sigem.comercial.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.Tarifa;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "alquiler_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AlquilerDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "alquiler_id", nullable = false)
    private Alquiler alquiler;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;

    // TARIFA APLICADA EN EL MOMENTO DEL ALQUILER (Snapshot)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tarifa_id")
    private Tarifa tarifa;

    @Column(name = "unidades_tiempo", nullable = false)
    private Integer unidadesTiempo; // Horas, días, semanas o meses según tipo_tarifa

    @Column(name = "precio_unitario", nullable = false, precision = 12, scale = 2)
    private BigDecimal precioUnitario; // Precio congelado al momento del alquiler

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal; // unidades_tiempo * precio_unitario
}