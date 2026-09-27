package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Almacen;
import com.transmaqsur.sigem.model.Cliente;
import com.transmaqsur.sigem.model.Operacion;
import com.transmaqsur.sigem.model.Repuesto;
import com.transmaqsur.sigem.model.Venta;
import com.transmaqsur.sigem.model.VentaDetalle;
import com.transmaqsur.sigem.model.enums.EstadoVenta;
import com.transmaqsur.sigem.model.enums.TipoComprobante;
import com.transmaqsur.sigem.model.enums.TipoDocumento;
import com.transmaqsur.sigem.model.enums.TipoLineaVenta;
import com.transmaqsur.sigem.repository.VentaDetalleRepository;
import com.transmaqsur.sigem.repository.VentaRepository;
import com.transmaqsur.sigem.util.Entidades;
import com.transmaqsur.sigem.util.Montos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * Ventas y facturación: comprobantes por alquileres y servicios finalizados
 * (horas/km facturables), venta de repuestos y otros conceptos.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class VentaService {

    private static final int DIAS_CREDITO = 30;

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository detalleRepository;
    private final AlquilerService alquilerService;
    private final ServicioTransporteService servicioService;
    private final InventarioService inventarioService;
    private final ClienteService clienteService;

    @Transactional(readOnly = true)
    public List<Venta> listar() {
        return ventaRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public Venta obtener(Long id) {
        return ventaRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Venta", id));
    }

    public Venta guardar(Venta form) {
        validarComprobante(form.getTipoComprobante(), form.getCliente());
        if (form.isNuevo()) {
            form.setEstado(EstadoVenta.BORRADOR);
            return ventaRepository.save(form);
        }
        Venta v = obtener(form.getId());
        exigirBorrador(v);
        if (!v.getCliente().getId().equals(form.getCliente().getId()) && v.getDetalles().stream().anyMatch(this::esOperacion)) {
            throw new NegocioException("No puede cambiar el cliente: el comprobante incluye operaciones del cliente actual");
        }
        Entidades.copiar(form, v, "serie", "numero", "estado", "subtotal", "igv", "total", "detalles", "fechaPago", "metodoPago",
                "fechaVencimiento");
        return v;
    }

    /** Crea un borrador con la operación indicada (atajo desde el alquiler o servicio). */
    public Venta facturarOperacion(String tipo, Long operacionId) {
        Operacion op = operacion(tipo, operacionId);
        Venta v = new Venta();
        v.setCliente(op.getCliente());
        v.setTipoComprobante(op.getCliente().getTipoDocumento() == TipoDocumento.RUC ? TipoComprobante.FACTURA : TipoComprobante.BOLETA);
        v = guardar(v);
        agregarOperacion(v.getId(), tipo + "-" + operacionId);
        return v;
    }

    /** Agrega una línea por un alquiler ("A-id") o servicio ("S-id") finalizado y no facturado. */
    public void agregarOperacion(Long ventaId, String referencia) {
        Venta v = obtener(ventaId);
        exigirBorrador(v);
        if (referencia == null || !referencia.matches("[AS]-\\d+")) {
            throw new NegocioException("Seleccione el alquiler o servicio a facturar");
        }
        String tipo = referencia.substring(0, 1);
        Long id = Long.valueOf(referencia.substring(2));
        Operacion op = operacion(tipo, id);
        if (!op.isFacturable()) {
            throw new NegocioException(op.getCodigo() + " no está finalizado o ya fue facturado");
        }
        if (!op.getCliente().getId().equals(v.getCliente().getId())) {
            throw new NegocioException(op.getCodigo() + " pertenece a otro cliente");
        }
        boolean yaIncluida = "A".equals(tipo)
                ? detalleRepository.existsByAlquilerIdAndVentaEstadoNot(id, EstadoVenta.ANULADA)
                : detalleRepository.existsByServicioIdAndVentaEstadoNot(id, EstadoVenta.ANULADA);
        if (yaIncluida) {
            throw new NegocioException(op.getCodigo() + " ya está incluido en otro comprobante");
        }
        VentaDetalle d = nuevaLinea(v, "A".equals(tipo) ? TipoLineaVenta.ALQUILER : TipoLineaVenta.SERVICIO, op.getConcepto(),
                op.getCantidadFacturable(), op.getModalidad().getUnidad().toUpperCase(), op.getTarifa());
        if ("A".equals(tipo)) {
            d.setAlquiler(alquilerService.obtener(id));
        } else {
            d.setServicio(servicioService.obtener(id));
        }
        recalcular(v);
    }

    public void agregarRepuesto(Long ventaId, Long repuestoId, Long almacenId, BigDecimal cantidad, BigDecimal precio) {
        Venta v = obtener(ventaId);
        exigirBorrador(v);
        validarCantidadPrecio(cantidad, precio);
        Repuesto r = inventarioService.obtenerRepuesto(repuestoId);
        Almacen a = inventarioService.obtenerAlmacen(almacenId);
        BigDecimal disponible = inventarioService.stock(almacenId, repuestoId);
        if (disponible.compareTo(cantidad) < 0) {
            throw new NegocioException("Stock insuficiente de " + r.getNombre() + " en " + a.getNombre() + ": disponible "
                    + disponible.stripTrailingZeros().toPlainString());
        }
        VentaDetalle d = nuevaLinea(v, TipoLineaVenta.REPUESTO, r.getDescripcionCompleta(), cantidad, r.getUnidadMedida(),
                precio != null ? precio : r.getPrecioUnitario());
        d.setRepuesto(r);
        d.setAlmacen(a);
        recalcular(v);
    }

    public void agregarConcepto(Long ventaId, String descripcion, BigDecimal cantidad, BigDecimal precio) {
        Venta v = obtener(ventaId);
        exigirBorrador(v);
        if (descripcion == null || descripcion.isBlank()) {
            throw new NegocioException("Ingrese la descripción del concepto");
        }
        if (precio == null) {
            throw new NegocioException("Ingrese el precio unitario");
        }
        validarCantidadPrecio(cantidad, precio);
        nuevaLinea(v, TipoLineaVenta.OTRO, descripcion, cantidad, "UND", precio);
        recalcular(v);
    }

    public void quitarLinea(Long ventaId, Long detalleId) {
        Venta v = obtener(ventaId);
        exigirBorrador(v);
        v.getDetalles().removeIf(d -> d.getId().equals(detalleId));
        recalcular(v);
    }

    /** Emite el comprobante: asigna correlativo, descuenta repuestos y marca las operaciones como facturadas. */
    public void emitir(Long id) {
        Venta v = obtener(id);
        exigirBorrador(v);
        if (v.getDetalles().isEmpty()) {
            throw new NegocioException("El comprobante no tiene líneas");
        }
        validarComprobante(v.getTipoComprobante(), v.getCliente());
        v.setSerie(v.getTipoComprobante().getSerie());
        v.setNumero(ventaRepository.ultimoNumero(v.getTipoComprobante()) + 1);
        for (VentaDetalle d : v.getDetalles()) {
            if (d.getRepuesto() != null) {
                inventarioService.salida(d.getAlmacen(), d.getRepuesto(), d.getCantidad(), v.getNumeroComprobante(),
                        "Venta a " + v.getCliente().getRazonSocial());
            }
            Operacion op = d.getAlquiler() != null ? d.getAlquiler() : d.getServicio();
            if (op != null) {
                if (!op.isFacturable()) {
                    throw new NegocioException(op.getCodigo() + " ya fue facturado");
                }
                op.setFacturado(true);
            }
        }
        v.setFechaVencimiento(v.getFechaEmision().plusDays(DIAS_CREDITO));
        v.setEstado(EstadoVenta.EMITIDA);
    }

    public void registrarPago(Long id, String metodoPago, LocalDate fechaPago) {
        Venta v = obtener(id);
        if (v.getEstado() != EstadoVenta.EMITIDA) {
            throw new NegocioException("Solo se registra el pago de comprobantes emitidos");
        }
        if (metodoPago == null || metodoPago.isBlank()) {
            throw new NegocioException("Indique el método de pago");
        }
        v.setMetodoPago(metodoPago);
        v.setFechaPago(fechaPago != null ? fechaPago : LocalDate.now());
        v.setEstado(EstadoVenta.PAGADA);
    }

    /** Anula un comprobante emitido: devuelve los repuestos y libera las operaciones para refacturar. */
    public void anular(Long id) {
        Venta v = obtener(id);
        if (v.getEstado() == EstadoVenta.BORRADOR) {
            ventaRepository.delete(v);
            return;
        }
        if (v.getEstado() == EstadoVenta.ANULADA) {
            throw new NegocioException("El comprobante ya está anulado");
        }
        for (VentaDetalle d : v.getDetalles()) {
            if (d.getRepuesto() != null) {
                inventarioService.entrada(d.getAlmacen(), d.getRepuesto(), d.getCantidad(), v.getNumeroComprobante(),
                        "Anulación de " + v.getNumeroComprobante());
            }
            Operacion op = d.getAlquiler() != null ? d.getAlquiler() : d.getServicio();
            if (op != null) {
                op.setFacturado(false);
            }
        }
        v.setEstado(EstadoVenta.ANULADA);
    }

    /** Operaciones finalizadas pendientes de facturar del cliente, con su referencia "A-id" / "S-id". */
    @Transactional(readOnly = true)
    public List<Operacion> pendientesDeFacturar(Long clienteId) {
        List<Operacion> lista = new java.util.ArrayList<>();
        alquilerService.pendientesDeFacturar(clienteId).stream()
                .filter(a -> !detalleRepository.existsByAlquilerIdAndVentaEstadoNot(a.getId(), EstadoVenta.ANULADA)).forEach(lista::add);
        servicioService.pendientesDeFacturar(clienteId).stream()
                .filter(s -> !detalleRepository.existsByServicioIdAndVentaEstadoNot(s.getId(), EstadoVenta.ANULADA)).forEach(lista::add);
        return lista;
    }

    private Operacion operacion(String tipo, Long id) {
        return switch (tipo) {
            case "A" -> alquilerService.obtener(id);
            case "S" -> servicioService.obtener(id);
            default -> throw new NegocioException("Tipo de operación desconocido: " + tipo);
        };
    }

    private boolean esOperacion(VentaDetalle d) {
        return d.getAlquiler() != null || d.getServicio() != null;
    }

    private VentaDetalle nuevaLinea(Venta v, TipoLineaVenta tipo, String descripcion, BigDecimal cantidad, String um, BigDecimal precio) {
        VentaDetalle d = new VentaDetalle();
        d.setVenta(v);
        d.setTipoLinea(tipo);
        d.setDescripcion(descripcion.length() > 300 ? descripcion.substring(0, 300) : descripcion);
        d.setCantidad(cantidad);
        d.setUnidadMedida(um.length() > 10 ? um.substring(0, 10) : um);
        d.setPrecioUnitario(precio);
        d.setSubtotal(Montos.multiplicar(cantidad, precio));
        v.getDetalles().add(d);
        return d;
    }

    private void recalcular(Venta v) {
        BigDecimal subtotal = v.getDetalles().stream().map(VentaDetalle::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        v.setSubtotal(Montos.redondear(subtotal));
        v.setIgv(Montos.igv(subtotal));
        v.setTotal(v.getSubtotal().add(v.getIgv()));
    }

    private void validarComprobante(TipoComprobante tipo, Cliente cliente) {
        Cliente c = clienteService.obtener(cliente.getId());
        if (tipo == TipoComprobante.FACTURA && c.getTipoDocumento() != TipoDocumento.RUC) {
            throw new NegocioException("Para emitir factura el cliente debe tener RUC; emita una boleta");
        }
    }

    private void validarCantidadPrecio(BigDecimal cantidad, BigDecimal precio) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new NegocioException("La cantidad debe ser mayor que cero");
        }
        if (precio != null && precio.signum() < 0) {
            throw new NegocioException("El precio no puede ser negativo");
        }
    }

    private void exigirBorrador(Venta v) {
        if (!v.isEditable()) {
            throw new NegocioException("El comprobante " + v.getNumeroComprobante() + " ya fue emitido y no se puede modificar");
        }
    }
}
