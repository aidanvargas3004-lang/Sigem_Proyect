package com.transur.sigem.maquinaria.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "tarifas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Tarifa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id")
    private Maquinaria maquinaria;

    // TIPO DE TARIFA: Controlado por CHECK en BD
    @Column(name = "tipo_tarifa", nullable = false, length = 20)
    private String tipoTarifa;
    // Valores: HORA, DIA, SEMANA, MES

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal precio;

    @Column(name = "vigencia_inicio", nullable = false)
    private LocalDate vigenciaInicio;

    @Column(name = "vigencia_fin")
    private LocalDate vigenciaFin;
}