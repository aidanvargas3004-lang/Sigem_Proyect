package com.transur.sigem.activos.entity;

import com.transur.sigem.compras.entity.Proveedor; // Importar desde el módulo de compras
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "adquisiciones_maquinaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AdquisicionMaquinaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proveedor_id", nullable = false)
    private Proveedor proveedor;

    @Column(name = "tipo_adquisicion", nullable = false, length = 30)
    private String tipoAdquisicion;
    // COMPRA_DIRECTA, LEASING, FINANCIAMIENTO_BANCARIO, DONACION, PERMUTA

    @Column(name = "costo_total", nullable = false, precision = 15, scale = 2)
    private BigDecimal costoTotal;

    @Column(name = "fecha_adquisicion", nullable = false)
    private LocalDate fechaAdquisicion = LocalDate.now();

    @Column(name = "numero_serie_fabrica", length = 100)
    private String numeroSerieFabrica;

    @Column(name = "documentos_legal_url", length = 255)
    private String documentosLegalUrl;

    @Column(name = "destino_inicial", nullable = false, length = 30)
    private String destinoInicial;
    // VENTA, ALQUILER, USO_INTERNO

    @Column(nullable = false, length = 30)
    private String estado = "ACTIVA";

    @OneToMany(mappedBy = "adquisicion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DetalleAdquisicion> detalles;
}