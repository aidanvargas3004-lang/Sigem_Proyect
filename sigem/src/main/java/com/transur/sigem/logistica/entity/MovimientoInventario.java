package com.transur.sigem.logistica.entity;

import com.transur.sigem.security.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "movimientos_inventario")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MovimientoInventario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repuesto_id", nullable = false)
    private Repuesto repuesto;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private UbicacionAlmacen ubicacion;

    // TIPO DE MOVIMIENTO: Controlado por CHECK en BD
    @Column(name = "tipo_movimiento", nullable = false, length = 30)
    private String tipoMovimiento;
    // Valores: ENTRADA, SALIDA, AJUSTE, DEVOLUCION

    @Column(nullable = false)
    private Integer cantidad; // Siempre positivo, el signo lo da tipoMovimiento

    @Column(columnDefinition = "TEXT")
    private String motivo;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Quién registró el movimiento
}