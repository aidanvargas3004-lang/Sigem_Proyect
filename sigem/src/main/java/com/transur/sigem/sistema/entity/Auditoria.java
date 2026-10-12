package com.transur.sigem.sistema.entity;

import com.transur.sigem.security.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "auditoria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Auditoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN: Quién ejecutó la acción
    // ON DELETE SET NULL: Preservamos el registro auditor incluso si se borra al usuario
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 100)
    private String modulo; // EJ: MAQUINARIA, VENTAS, RRHH, LOGISTICA

    @Column(nullable = false, length = 100)
    private String accion; // EJ: CREATE, UPDATE, DELETE, LOGIN, EXPORTAR

    @Column(length = 100)
    private String entidad; // EJ: Maquinaria, Contrato, Empleado

    @Column(name = "entidad_id")
    private Long entidadId; // ID del registro afectado

    // DATOS ANTERIORES AL CAMBIO (Formato JSON nativo de PostgreSQL)
    @Column(name = "datos_anteriores", columnDefinition = "jsonb")
    private String datosAnteriores;

    // DATOS NUEVOS DESPUÉS DEL CAMBIO
    @Column(name = "datos_nuevos", columnDefinition = "jsonb")
    private String datosNuevos;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}