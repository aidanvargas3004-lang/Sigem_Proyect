package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.EstadoCompra;
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

/** Orden de compra de repuestos a un proveedor. */
@Getter
@Setter
@Entity
@Table(name = "compra")
public class Compra extends EntidadBase {

    @Column(unique = true, length = 20)
    private String codigo;

    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "proveedor_id")
    private Proveedor proveedor;

    /** Almacén donde ingresará la mercadería. */
    @NotNull
    @ManyToOne(optional = false)
    @JoinColumn(name = "almacen_id")
    private Almacen almacen;

    @NotNull
    private LocalDate fechaEmision = LocalDate.now();

    private LocalDate fechaRecepcion;

    @Size(max = 30)
    @Column(length = 30)
    private String comprobanteProveedor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoCompra estado = EstadoCompra.PENDIENTE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal igv = BigDecimal.ZERO;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total = BigDecimal.ZERO;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observaciones;

    @OneToMany(mappedBy = "compra", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id")
    private List<CompraDetalle> detalles = new ArrayList<>();

    public boolean isEditable() {
        return estado == EstadoCompra.PENDIENTE;
    }
}
