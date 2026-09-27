package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.FuenteGps;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/** Posición GPS reportada por una unidad. */
@Getter
@Setter
@Entity
@SinAuditoria
@Table(name = "ubicacion_gps", indexes = @Index(name = "idx_gps_unidad_fecha", columnList = "unidad_id, fechaHora"))
public class UbicacionGps extends EntidadBase {

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    @Column(nullable = false)
    private Double latitud;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    @Column(nullable = false)
    private Double longitud;

    @PositiveOrZero
    private Double velocidad;

    @NotNull
    @Column(nullable = false)
    private LocalDateTime fechaHora = LocalDateTime.now();

    @Size(max = 200)
    @Column(length = 200)
    private String referencia;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private FuenteGps fuente = FuenteGps.MANUAL;
}
