package com.transur.sigem.operaciones.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "incidencias")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Incidencia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id", nullable = false)
    private Servicio servicio;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id") // Opcional: puede ser incidencia general del servicio
    private Maquinaria maquinaria;

    @Column(nullable = false, length = 50)
    private String tipo; // MECANICA, ACCIDENTE, CLIMA, TRAFICO, SEGURIDAD, OTRO

    @Column(nullable = false, columnDefinition = "TEXT")
    private String descripcion; // Detalle completo de lo ocurrido

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();

    @Column(nullable = false, length = 30)
    private String estado = "ABIERTA";
    // ABIERTA, EN_PROCESO, RESUELTA, CERRADA

    @Column(name = "foto_evidencia_url", length = 500)
    private String fotoEvidenciaUrl; // Foto de la falla/accidente
}