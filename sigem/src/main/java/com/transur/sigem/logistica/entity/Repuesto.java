package com.transur.sigem.logistica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "repuestos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Repuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id")
    private CategoriaRepuesto categoria;

    @Column(nullable = false, unique = true, length = 30)
    private String codigo; // SKU del repuesto

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    // STOCK ACTUAL: Validado por CHECK (>= 0) en BD
    @Column(name = "stock_actual", nullable = false)
    private Integer stockActual = 0;

    // STOCK MINIMO: Umbral para alertas automáticas
    @Column(name = "stock_minimo", nullable = false)
    private Integer stockMinimo = 0;

    @Column(name = "unidad_medida", length = 30)
    private String unidadMedida; // UNIDAD, LITRO, KG, PAR

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(name = "imagen_url", length = 500)
    private String imagenUrl; // Foto del repuesto para catálogo visual
}