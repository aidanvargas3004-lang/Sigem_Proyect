package com.transur.sigem.mantenimiento.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Plan de mantenimiento preventivo de una unidad (RF012): se repite cada cierta
 * cantidad de horas de horómetro, kilómetros o días calendario.
 */
@Entity
@Table(name = "planes_mantenimiento")
@Getter
@Setter
@NoArgsConstructor
public class PlanMantenimiento {

    public static final String HOROMETRO = "HOROMETRO";
    public static final String KILOMETRAJE = "KILOMETRAJE";
    public static final String FECHA = "FECHA";

    /** Se alerta cuando se consume el 90 % de la frecuencia. */
    private static final BigDecimal UMBRAL = new BigDecimal("0.90");

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "maquinaria_id", nullable = false)
    private Maquinaria maquinaria;

    /** Ej.: "Cambio de aceite y filtros", "Revisión de tren de rodaje". */
    @Column(nullable = false, length = 150)
    private String actividad;

    /** HOROMETRO, KILOMETRAJE o FECHA. */
    @Column(nullable = false, length = 20)
    private String criterio;

    /** Cada cuántas horas, kilómetros o días se repite. */
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal frecuencia;

    /** Lectura (horas o km) del último mantenimiento realizado. */
    @Column(name = "ultimo_valor", precision = 12, scale = 2)
    private BigDecimal ultimoValor = BigDecimal.ZERO;

    @Column(name = "ultima_fecha")
    private LocalDate ultimaFecha;

    @Column(nullable = false)
    private Boolean activo = true;

    public boolean porFecha() {
        return FECHA.equals(criterio);
    }

    /** Lectura actual de la unidad según el criterio del plan. */
    public BigDecimal lecturaActual() {
        if (KILOMETRAJE.equals(criterio)) {
            return nvl(maquinaria.getKilometrajeActual());
        }
        return nvl(maquinaria.getHorometroActual());
    }

    public BigDecimal getProximoValor() {
        return porFecha() ? null : nvl(ultimoValor).add(frecuencia);
    }

    public LocalDate getProximaFecha() {
        if (!porFecha()) {
            return null;
        }
        LocalDate base = ultimaFecha != null ? ultimaFecha : LocalDate.now();
        return base.plusDays(frecuencia.longValue());
    }

    /** Porcentaje de la frecuencia consumido desde el último mantenimiento. */
    public int getPorcentaje() {
        if (frecuencia == null || frecuencia.signum() == 0) {
            return 0;
        }
        BigDecimal consumido;
        if (porFecha()) {
            LocalDate base = ultimaFecha != null ? ultimaFecha : LocalDate.now();
            consumido = BigDecimal.valueOf(ChronoUnit.DAYS.between(base, LocalDate.now()));
        } else {
            consumido = lecturaActual().subtract(nvl(ultimoValor));
        }
        return consumido.multiply(BigDecimal.valueOf(100)).divide(frecuencia, 0, RoundingMode.HALF_UP).intValue();
    }

    /** OK, PROXIMO (≥ 90 %) o VENCIDO (≥ 100 %). */
    public String getSituacion() {
        int p = getPorcentaje();
        if (p >= 100) {
            return "VENCIDO";
        }
        return p >= UMBRAL.multiply(BigDecimal.valueOf(100)).intValue() ? "PROXIMO" : "OK";
    }

    public String getUnidad() {
        return switch (criterio) {
            case KILOMETRAJE -> "km";
            case FECHA -> "días";
            default -> "h";
        };
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
