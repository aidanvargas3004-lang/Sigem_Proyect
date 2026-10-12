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

    /** Fin programado: con el inicio define el rango que se valida contra otras asignaciones. */
    @Column(name = "fecha_fin_programada")
    private LocalDateTime fechaFinProgramada;

    // ---- Ejecución real del servicio (RF010: inicio y cierre)
    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal;

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal;

    @Column(name = "horometro_inicial", precision = 10, scale = 2)
    private java.math.BigDecimal horometroInicial;

    @Column(name = "horometro_final", precision = 10, scale = 2)
    private java.math.BigDecimal horometroFinal;

    @Column(name = "km_inicial", precision = 12, scale = 2)
    private java.math.BigDecimal kmInicial;

    @Column(name = "km_final", precision = 12, scale = 2)
    private java.math.BigDecimal kmFinal;

    /** Horas de uso facturables = horómetro final − inicial. */
    @Column(name = "horas_uso", precision = 10, scale = 2)
    private java.math.BigDecimal horasUso;

    /** Kilómetros recorridos = odómetro final − inicial. */
    @Column(name = "km_recorridos", precision = 12, scale = 2)
    private java.math.BigDecimal kmRecorridos;

    /** Fin efectivo del rango ocupado (si no hay fin programado se asume una jornada de 12 h). */
    public LocalDateTime getFinOcupacion() {
        if (fechaFinProgramada != null) {
            return fechaFinProgramada;
        }
        return fechaProgramada == null ? null : fechaProgramada.plusHours(12);
    }

    @Column(nullable = false, length = 30)
    private String estado = "POR_ASIGNAR";
    // POR_ASIGNAR, ASIGNADO, EN_CURSO, FINALIZADO, SUSPENDIDO, CANCELADO

    @Column(columnDefinition = "TEXT")
    private String observaciones; // Instrucciones especiales, condiciones de campo, etc.

    @Column(name = "foto_checklist_url", length = 500)
    private String fotoChecklistUrl; // Checklist firmado/fotografiado al finalizar
}
