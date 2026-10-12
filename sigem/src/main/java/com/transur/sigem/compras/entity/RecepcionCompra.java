package com.transur.sigem.compras.entity;

import com.transur.sigem.security.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "recepciones_compra")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionCompra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_compra_id", nullable = false)
    private OrdenCompra ordenCompra;

    @Column(nullable = false, unique = true, length = 30)
    private String numero; // Número de guía/recepción

    @Column(name = "fecha_recepcion", nullable = false)
    private LocalDate fechaRecepcion = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @Column(nullable = false, length = 30)
    private String estado = "RECIBIDA"; // RECIBIDA, PARCIAL, RECHAZADA

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Quién recibió la mercadería

    // RELACIÓN: Una recepción puede tener múltiples líneas de detalle
    @OneToMany(mappedBy = "recepcion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RecepcionDetalle> detalles;
}