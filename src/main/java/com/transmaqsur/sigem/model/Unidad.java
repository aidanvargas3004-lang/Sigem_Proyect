package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Unidad de la flota: vehículo de carga o maquinaria pesada. */
@Getter
@Setter
@Entity
@Table(name = "unidad")
public class Unidad extends EntidadBase {

    /** Porcentaje del intervalo a partir del cual se alerta el mantenimiento preventivo. */
    public static final BigDecimal UMBRAL_ALERTA = new BigDecimal("0.90");

    @NotBlank
    @Size(max = 20)
    @Column(nullable = false, unique = true, length = 20)
    private String codigo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoUnidad tipo;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String marca;

    @NotBlank
    @Size(max = 60)
    @Column(nullable = false, length = 60)
    private String modelo;

    @Min(1970)
    @Max(2100)
    private Integer anio;

    @Size(max = 10)
    @Column(length = 10)
    private String placa;

    @Size(max = 60)
    @Column(length = 60)
    private String numeroSerie;

    @Size(max = 60)
    @Column(length = 60)
    private String capacidad;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoUnidad estado = EstadoUnidad.DISPONIBLE;

    /** Lectura actual del odómetro (km) u horómetro (h) según el tipo. */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 1)
    private BigDecimal lecturaActual = BigDecimal.ZERO;

    /** Cada cuántos km u horas corresponde un mantenimiento preventivo. */
    @NotNull
    @Positive
    @Column(nullable = false, precision = 12, scale = 1)
    private BigDecimal intervaloMantenimiento;

    /** Lectura registrada en el último mantenimiento preventivo. */
    @NotNull
    @PositiveOrZero
    @Column(nullable = false, precision = 12, scale = 1)
    private BigDecimal lecturaUltimoMantenimiento = BigDecimal.ZERO;

    /** Frecuencia del preventivo por calendario, en días (opcional; se cumple lo que ocurra primero). */
    @Positive
    private Integer intervaloDiasMantenimiento;

    /** Fecha del último mantenimiento preventivo. */
    private LocalDate fechaUltimoMantenimiento;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal tarifaHora;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal tarifaDia;

    @PositiveOrZero
    @Column(precision = 10, scale = 2)
    private BigDecimal tarifaKm;

    private LocalDate fechaAdquisicion;

    private LocalDate vencimientoSoat;

    private LocalDate vencimientoRevisionTecnica;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observaciones;

    public String getDescripcion() {
        return codigo + " - " + (tipo != null ? tipo.getLabel() : "") + " " + marca + " " + modelo
                + (placa != null && !placa.isBlank() ? " (" + placa + ")" : "");
    }

    public BigDecimal getProximoMantenimiento() {
        return lecturaUltimoMantenimiento.add(intervaloMantenimiento);
    }

    /** Km u horas que faltan para el siguiente preventivo (negativo si está vencido). */
    public BigDecimal getRestanteMantenimiento() {
        return getProximoMantenimiento().subtract(lecturaActual);
    }

    /** Porcentaje del intervalo consumido desde el último preventivo. */
    public int getPorcentajeUso() {
        if (intervaloMantenimiento == null || intervaloMantenimiento.signum() == 0) {
            return 0;
        }
        return lecturaActual.subtract(lecturaUltimoMantenimiento)
                .multiply(BigDecimal.valueOf(100))
                .divide(intervaloMantenimiento, 0, RoundingMode.HALF_UP).intValue();
    }

    /** Fecha en que corresponde el próximo preventivo por calendario (null si no se usa). */
    public LocalDate getProximaFechaMantenimiento() {
        if (intervaloDiasMantenimiento == null || fechaUltimoMantenimiento == null) {
            return null;
        }
        return fechaUltimoMantenimiento.plusDays(intervaloDiasMantenimiento);
    }

    /** Días que faltan para el preventivo por calendario (negativo si está vencido). */
    public Long getDiasParaMantenimiento() {
        LocalDate proxima = getProximaFechaMantenimiento();
        return proxima == null ? null : ChronoUnit.DAYS.between(LocalDate.now(), proxima);
    }

    private boolean isAlertaPorUso() {
        return lecturaActual.subtract(lecturaUltimoMantenimiento)
                .compareTo(intervaloMantenimiento.multiply(UMBRAL_ALERTA)) >= 0;
    }

    /** Alerta por calendario cuando queda el 10 % del intervalo de días (mínimo 3 días). */
    public boolean isAlertaPorFecha() {
        Long dias = getDiasParaMantenimiento();
        return dias != null && dias <= Math.max(3, Math.round(intervaloDiasMantenimiento * 0.1));
    }

    /** Requiere preventivo por km/horas o por fecha, lo que ocurra primero. */
    public boolean isRequiereMantenimiento() {
        return isAlertaPorUso() || isAlertaPorFecha();
    }

    public boolean isMantenimientoVencido() {
        Long dias = getDiasParaMantenimiento();
        return getRestanteMantenimiento().signum() <= 0 || (dias != null && dias <= 0);
    }
}
