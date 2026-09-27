package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Compra;
import com.transmaqsur.sigem.model.CompraDetalle;
import com.transmaqsur.sigem.model.Proveedor;
import com.transmaqsur.sigem.model.Repuesto;
import com.transmaqsur.sigem.model.enums.EstadoCompra;
import com.transmaqsur.sigem.repository.CompraRepository;
import com.transmaqsur.sigem.repository.ProveedorRepository;
import com.transmaqsur.sigem.util.Entidades;
import com.transmaqsur.sigem.util.Montos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/** Proveedores y órdenes de compra: pendiente → aprobada → recibida (ingresa al almacén). */
@Service
@RequiredArgsConstructor
@Transactional
public class CompraService {

    private final ProveedorRepository proveedorRepository;
    private final CompraRepository compraRepository;
    private final InventarioService inventarioService;

    // ------------------------------------------------------------ Proveedores

    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedores() {
        return proveedorRepository.findAllByOrderByRazonSocialAsc();
    }

    @Transactional(readOnly = true)
    public List<Proveedor> listarProveedoresActivos() {
        return proveedorRepository.findByActivoTrueOrderByRazonSocialAsc();
    }

    @Transactional(readOnly = true)
    public Proveedor obtenerProveedor(Long id) {
        return proveedorRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Proveedor", id));
    }

    public Proveedor guardarProveedor(Proveedor form) {
        Long id = form.isNuevo() ? 0L : form.getId();
        if (proveedorRepository.existsByRucAndIdNot(form.getRuc(), id)) {
            throw new NegocioException("Ya existe un proveedor con RUC " + form.getRuc());
        }
        if (form.isNuevo()) {
            return proveedorRepository.save(form);
        }
        Proveedor p = obtenerProveedor(form.getId());
        Entidades.copiar(form, p);
        return p;
    }

    // ------------------------------------------------------------ Órdenes de compra

    @Transactional(readOnly = true)
    public List<Compra> listar() {
        return compraRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Compra obtener(Long id) {
        return compraRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Orden de compra", id));
    }

    public Compra guardar(Compra form) {
        if (form.isNuevo()) {
            form.setEstado(EstadoCompra.PENDIENTE);
            Compra c = compraRepository.save(form);
            c.setCodigo(Entidades.codigo("OC", c.getId()));
            return c;
        }
        Compra c = obtener(form.getId());
        exigirEditable(c);
        Entidades.copiar(form, c, "codigo", "estado", "subtotal", "igv", "total", "detalles", "fechaRecepcion");
        return c;
    }

    public void agregarDetalle(Long compraId, Long repuestoId, BigDecimal cantidad, BigDecimal precio) {
        Compra c = obtener(compraId);
        exigirEditable(c);
        if (cantidad == null || cantidad.signum() <= 0 || precio == null || precio.signum() < 0) {
            throw new NegocioException("Ingrese una cantidad mayor que cero y un precio válido");
        }
        Repuesto r = inventarioService.obtenerRepuesto(repuestoId);
        CompraDetalle d = new CompraDetalle();
        d.setCompra(c);
        d.setRepuesto(r);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precio);
        d.setSubtotal(Montos.multiplicar(cantidad, precio));
        c.getDetalles().add(d);
        recalcular(c);
    }

    public void quitarDetalle(Long compraId, Long detalleId) {
        Compra c = obtener(compraId);
        exigirEditable(c);
        c.getDetalles().removeIf(d -> d.getId().equals(detalleId));
        recalcular(c);
    }

    public void aprobar(Long id) {
        Compra c = obtener(id);
        exigirEditable(c);
        if (c.getDetalles().isEmpty()) {
            throw new NegocioException("Agregue al menos un repuesto antes de aprobar la orden");
        }
        c.setEstado(EstadoCompra.APROBADA);
    }

    /** Recepción de la mercadería: ingresa el stock al almacén y actualiza el último costo. */
    public void recibir(Long id, String comprobanteProveedor) {
        Compra c = obtener(id);
        if (c.getEstado() != EstadoCompra.APROBADA) {
            throw new NegocioException("Solo se pueden recibir órdenes aprobadas");
        }
        for (CompraDetalle d : c.getDetalles()) {
            inventarioService.entrada(c.getAlmacen(), d.getRepuesto(), d.getCantidad(), c.getCodigo(),
                    "Compra a " + c.getProveedor().getRazonSocial());
            d.getRepuesto().setPrecioUnitario(d.getPrecioUnitario());
        }
        c.setComprobanteProveedor(comprobanteProveedor);
        c.setFechaRecepcion(LocalDate.now());
        c.setEstado(EstadoCompra.RECIBIDA);
    }

    public void anular(Long id) {
        Compra c = obtener(id);
        if (c.getEstado() == EstadoCompra.RECIBIDA || c.getEstado() == EstadoCompra.ANULADA) {
            throw new NegocioException("No se puede anular una orden " + c.getEstado().getLabel().toLowerCase());
        }
        c.setEstado(EstadoCompra.ANULADA);
    }

    private void recalcular(Compra c) {
        BigDecimal subtotal = c.getDetalles().stream().map(CompraDetalle::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        c.setSubtotal(Montos.redondear(subtotal));
        c.setIgv(Montos.igv(subtotal));
        c.setTotal(c.getSubtotal().add(c.getIgv()));
    }

    private void exigirEditable(Compra c) {
        if (!c.isEditable()) {
            throw new NegocioException("La orden " + c.getCodigo() + " ya no se puede modificar (" + c.getEstado().getLabel() + ")");
        }
    }
}
