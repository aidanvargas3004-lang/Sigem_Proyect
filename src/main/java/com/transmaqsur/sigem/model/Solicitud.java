package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoSolicitud;
import com.transmaqsur.sigem.model.enums.TipoServicio;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

/** Requerimiento de un cliente (alquiler de maquinaria o transporte de carga). */
@Getter
@Setter
@Entity
@Table(name = "solicitud")
public class Solicitud extends EntidadBase {

    @Column(unique = true, length = 20)
    private String codigo;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @ManyToOne
    @JoinColumn(name = "contacto_id")
    private Contacto contacto;

    @NotNull
    private LocalDate fechaSolicitud = LocalDate.now();

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoServicio tipoServicio;

    @Enumerated(EnumType.STRING)
    @Column(length = 30)
    private TipoUnidad tipoUnidad;

    @Min(1)
    private int cantidadUnidades = 1;

    @NotNull
    private LocalDate fechaInicio;

    @NotNull
    private LocalDate fechaFin;

    /** Obra, origen de la carga o lugar de trabajo. */
    @Size(max = 200)
    @Column(length = 200)
    private String origen;

    @Size(max = 200)
    @Column(length = 200)
    private String destino;

    @NotBlank
    @Size(max = 1000)
    @Column(nullable = false, length = 1000)
    private String descripcion;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoSolicitud estado = EstadoSolicitud.REGISTRADA;
}
