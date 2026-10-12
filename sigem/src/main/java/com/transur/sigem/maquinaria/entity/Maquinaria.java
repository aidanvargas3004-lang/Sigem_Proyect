package com.transur.sigem.maquinaria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.util.List;

@Entity
@Table(name = "maquinarias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Maquinaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "codigo_interno", nullable = false, unique = true, length = 50)
    private String codigoInterno; // EJ: M-001, EXC-2024-05

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_id", nullable = false)
    private TipoMaquinaria tipo;

    @Column(nullable = false, length = 100)
    private String marca;

    @Column(nullable = false, length = 100)
    private String modelo;

    @Column
    private Integer anio;

    @Column(unique = true, length = 20)
    private String placa;

    @Column(name = "horometro_actual", precision = 10, scale = 2)
    private BigDecimal horometroActual = BigDecimal.ZERO;

    /** Odómetro para vehículos de carga (km). La maquinaria de obra usa solo el horómetro. */
    @Column(name = "kilometraje_actual", precision = 12, scale = 2)
    private BigDecimal kilometrajeActual = BigDecimal.ZERO;

    /** Documentación básica de la unidad (alertas de vencimiento, RF003 / RF014). */
    @Column(name = "soat_vencimiento")
    private java.time.LocalDate soatVencimiento;

    @Column(name = "revision_tecnica_vencimiento")
    private java.time.LocalDate revisionTecnicaVencimiento;

    // En Maquinaria.java
    @Column(name = "estado_operativo", nullable = false, length = 30)
    private String estadoOperativo = "OPERATIVO";
    // Valores: OPERATIVO, EN_MANTENIMIENTO_CORRECTIVO, VENDIDO, BAJA_DEFINITIVA, FUERA_DE_SERVICIO

    // DESTINO OPERATIVO: Define si se vende, alquila o usa para servicio interno
    @Column(name = "destino_operativo", nullable = false, length = 30)
    private String destinoOperativo = "MULTIPROPOSITO";
    // Valores: VENTA, ALQUILER, SERVICIO, MULTIPROPOSITO

    @Column(name = "precio_base_alquiler", precision = 12, scale = 2)
    private BigDecimal precioBaseAlquiler;

    @Column(name = "precio_base_venta", precision = 12, scale = 2)
    private BigDecimal precioBaseVenta;

    // RELACIÓN: Una maquinaria puede tener múltiples tarifas históricas/vigentes
    @OneToMany(mappedBy = "maquinaria", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Tarifa> tarifas;

    @Column(name = "imagen_url", length = 500)
    private String imageUrl; // URL HTTPS de Cloudinary
}