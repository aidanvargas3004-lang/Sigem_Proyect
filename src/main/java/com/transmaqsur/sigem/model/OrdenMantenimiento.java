package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoOrden;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoMantenimiento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** Orden de trabajo de mantenimiento preventivo o correctivo. */
@Getter
@Setter
@Entity
@Table(name = "orden_mantenimiento")
public class OrdenMantenimiento extends EntidadBase {

    @Column(unique = true, length = 20)
    private String codigo;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "unidad_id")
    private Unidad unidad;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoMantenimiento tipo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Prioridad prioridad = Prioridad.MEDIA;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String descripcion;

    @NotNull
    private LocalDate fechaProgramada;

    private LocalDateTime fechaInicio;

    private LocalDateTime fechaFin;

    /** Técnico responsable. */
    @ManyToOne
    @JoinColumn(name = "tecnico_id")
    private Empleado tecnico;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoOrden estado = EstadoOrden.PROGRAMADA;

    /** Lectura (km/h) de la unidad al momento del mantenimiento. */
    @Column(precision = 12, scale = 1)
    private BigDecimal lecturaUnidad;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoManoObra = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoRepuestos = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal costoServiciosTerceros = BigDecimal.ZERO;

    @Size(max = 2000)
    @Column(length = 2000)
    private String trabajoRealizado;

    @OneToMany(mappedBy = "orden", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<RepuestoUtilizado> repuestos = new ArrayList<>();

    public BigDecimal getCostoTotal() {
        return costoManoObra.add(costoRepuestos).add(costoServiciosTerceros);
    }

    public boolean isAbierta() {
        return estado == EstadoOrden.PROGRAMADA || estado == EstadoOrden.EN_PROCESO;
    }
}
