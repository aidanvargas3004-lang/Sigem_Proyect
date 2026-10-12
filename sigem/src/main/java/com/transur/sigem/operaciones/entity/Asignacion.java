package com.transur.sigem.operaciones.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.rrhh.entity.Empleado;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "asignaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Asignacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id") // Opcional: puede asignarse después
    private Empleado empleado; // Operador / Conductor responsable

    @Column(name = "fecha_asignacion", nullable = false)
    private LocalDateTime fechaAsignacion = LocalDateTime.now();

    @Column(nullable = false, length = 30)
    private String estado = "ACTIVA";

    public void setEmpleado(com.transur.sigem.rrhh.entity.Empleado empleado) {
        this.empleado = empleado;
    }
    // ACTIVA, COMPLETADA, REEMPLAZADA, CANCELADA
}
