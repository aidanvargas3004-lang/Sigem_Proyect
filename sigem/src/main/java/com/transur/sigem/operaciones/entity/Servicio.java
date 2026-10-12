package com.transur.sigem.operaciones.entity;

import com.transur.sigem.comercial.entity.Contrato;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "servicios")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Servicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN 1:1 ÚNICA CON CONTRATO DE SERVICIO
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private Contrato contrato;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tipo_servicio_id", nullable = false)
    private TipoServicio tipoServicio;

    @Column(length = 255)
    private String origen; // Punto de partida / ubicación inicial

    @Column(length = 255)
    private String destino; // Punto de llegada / ubicación final

    @Column(name = "fecha_programada", nullable = false)
    private LocalDateTime fechaProgramada; // Cuándo debe iniciarse el servicio

    @Column(nullable = false, length = 30)
    private String estado = "POR_ASIGNAR";
    // POR_ASIGNAR, ASIGNADO, EN_CURSO, FINALIZADO, SUSPENDIDO, CANCELADO

    @Column(columnDefinition = "TEXT")
    private String observaciones; // Instrucciones especiales, condiciones de campo, etc.

    @Column(name = "foto_checklist_url", length = 500)
    private String fotoChecklistUrl; // Checklist firmado/fotografiado al finalizar
}
