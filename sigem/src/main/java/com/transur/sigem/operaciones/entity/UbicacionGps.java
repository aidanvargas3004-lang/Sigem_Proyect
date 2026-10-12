package com.transur.sigem.operaciones.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Posición informada de una unidad (registro manual o desde la app/dispositivo).
 * No es telemetría en tiempo real, conforme al alcance del proyecto.
 */
@Entity
@Table(name = "ubicaciones_gps", indexes = @Index(name = "idx_gps_maquinaria_fecha", columnList = "maquinaria_id, fecha_hora"))
@Getter
@Setter
@NoArgsConstructor
public class UbicacionGps {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "servicio_id")
    private Servicio servicio;

    @Column(nullable = false)
    private Double latitud;

    @Column(nullable = false)
    private Double longitud;

    private Double velocidad;

    @Column(length = 200)
    private String referencia;

    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Column(name = "registrado_por", length = 60)
    private String registradoPor;
}
