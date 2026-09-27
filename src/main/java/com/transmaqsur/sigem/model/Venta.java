package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoVenta;
import com.transmaqsur.sigem.model.enums.TipoComprobante;
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
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/** Comprobante de venta (factura o boleta) por alquileres, servicios o repuestos. */
@Getter
@Setter
@Entity
@Table(name = "venta")
public class Venta extends EntidadBase {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoComprobante tipoComprobante = TipoComprobante.FACTURA;

    @Column(length = 4)
    private String serie;

    /** Correlativo asignado al emitir el comprobante. */
    private Integer numero;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "cliente_id")
    private Cliente cliente;

    @NotNull
    private LocalDate fechaEmision = LocalDate.now();

    private LocalDate fechaVencimiento;

    private LocalDate fechaPago;

    @Size(max = 30)
    @Column(length = 30)
    private String metodoPago;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoVenta estado = EstadoVenta.BORRADOR;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observaciones;

    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<VentaDetalle> detalles = new ArrayList<>();

    public String getNumeroComprobante() {
        return numero == null ? "BORRADOR-" + getId() : serie + "-" + String.format("%08d", numero);
    }

    public boolean isEditable() {
        return estado == EstadoVenta.BORRADOR;
    }
}
