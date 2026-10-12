package com.transur.sigem.comercial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "alquileres")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Alquiler {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN 1:1 ÚNICA CON CONTRATO DE ALQUILER
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private com.transur.sigem.comercial.entity.Contrato contrato;

    @Column(name = "fecha_inicio_real")
    private LocalDateTime fechaInicioReal; // Momento real en que se entregó la máquina

    @Column(name = "fecha_fin_real")
    private LocalDateTime fechaFinReal; // Momento real de devolución

    @Column(nullable = false, length = 30)
    private String estado = "PROGRAMADO";
    // PROGRAMADO, EN_CURSO, FINALIZADO, SUSPENDIDO, CANCELADO

    @Column(columnDefinition = "TEXT")
    private String observaciones; // Estado al devolver, daños, horas extras, etc.

    // RELACIÓN: Un alquiler puede incluir múltiples máquinas con distintas tarifas
    @OneToMany(mappedBy = "alquiler", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<com.transur.sigem.comercial.entity.AlquilerDetalle> detalles;
}