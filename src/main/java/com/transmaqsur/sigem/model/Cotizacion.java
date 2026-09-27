package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoCotizacion;
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
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cotizacion")
public class Cotizacion extends EntidadBase {

    @Column(unique = true, length = 20)
    private String codigo;

    @ManyToOne
    @JoinColumn(name = "solicitud_id")
    private Solicitud solicitud;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @NotNull
    private LocalDate fechaEmision = LocalDate.now();

    @Min(1)
    private int diasValidez = 15;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCotizacion estado = EstadoCotizacion.BORRADOR;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Size(max = 2000)
    @Column(length = 2000)
    private String condiciones;

    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<CotizacionDetalle> detalles = new ArrayList<>();

    public LocalDate getFechaVencimiento() {
        return fechaEmision.plusDays(diasValidez);
    }

    public boolean isEditable() {
        return estado == EstadoCotizacion.BORRADOR;
    }
}
