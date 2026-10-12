package com.transur.sigem.mantenimiento.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.rrhh.entity.Empleado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "mantenimientos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Mantenimiento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;

    // TÉCNICO RESPONSABLE (Opcional: puede ser mantenimiento externo)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id")
    private Empleado empleado;

    // TIPO DE MANTENIMIENTO: Controlado por CHECK en BD
    @Column(name = "tipo_mantenimiento", nullable = false, length = 30)
    private String tipoMantenimiento;
    // PREVENTIVO, CORRECTIVO

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion; // Detalle de la falla o tarea preventiva

    @Column(name = "fecha_programada")
    private LocalDate fechaProgramada; // Para preventivos

    @Column(name = "fecha_inicio")
    private LocalDate fechaInicio; // Cuándo se empezó realmente

    @Column(name = "fecha_fin")
    private LocalDate fechaFin; // Cuándo se completó

    @Column(precision = 12, scale = 2)
    private BigDecimal costo = BigDecimal.ZERO; // Costo total estimado/real

    @Column(nullable = false, length = 30)
    private String estado = "PROGRAMADO";
    // PROGRAMADO, EN_PROCESO, COMPLETADO, CANCELADO

    @Column(columnDefinition = "TEXT")
    private String observaciones; // Repuestos adicionales, notas del técnico, etc.

    // RELACIÓN: Un mantenimiento puede usar múltiples repuestos
    @OneToMany(mappedBy = "mantenimiento", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<MantenimientoRepuesto> repuestosUsados;
}