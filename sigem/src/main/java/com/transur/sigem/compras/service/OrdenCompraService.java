package com.transur.sigem.compras.service;

import com.transur.sigem.compras.dto.OrdenCompraDetalleDTO;
import com.transur.sigem.compras.dto.OrdenCompraRequest;
import com.transur.sigem.compras.entity.OrdenCompra;
import com.transur.sigem.compras.entity.OrdenCompraDetalle;
import com.transur.sigem.compras.entity.Proveedor;
import com.transur.sigem.compras.repository.OrdenCompraRepository;
import com.transur.sigem.compras.repository.ProveedorRepository;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OrdenCompraService {

    private final OrdenCompraRepository ocRepository;
    private final ProveedorRepository provRepository;
    private final RepuestoRepository repuestoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public OrdenCompra crear(OrdenCompraRequest request) {
        if (ocRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException("Ya existe una OC con ese número");
        }

        Proveedor prov = provRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        OrdenCompra oc = new OrdenCompra();
        oc.setProveedor(prov);
        oc.setNumero(request.getNumero());
        oc.setFechaEmision(request.getFechaEmision() != null ? request.getFechaEmision() : LocalDate.now());
        oc.setEstado("PENDIENTE");

        if (request.getUsuarioId() != null) {
            Usuario u = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            oc.setUsuario(u);
        }

        List<OrdenCompraDetalle> detallesList = new ArrayList<>();
        BigDecimal subtotalGeneral = BigDecimal.ZERO;

        for (OrdenCompraDetalleDTO dto : request.getDetalles()) {
            OrdenCompraDetalle det = new OrdenCompraDetalle();
            det.setOrdenCompra(oc);

            if (dto.getRepuestoId() != null) {
                Repuesto r = repuestoRepository.findById(dto.getRepuestoId())
                        .orElseThrow(() -> new RuntimeException("Repuesto no encontrado"));
                det.setRepuesto(r);
                // Si viene repuesto, usamos su descripción por defecto si no se envía otra
                det.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion() : r.getNombre());
            } else {
                det.setDescripcion(dto.getDescripcion());
            }

            det.setCantidad(dto.getCantidad());
            det.setPrecioUnitario(dto.getPrecioUnitario());
            det.setSubtotal(dto.getPrecioUnitario().multiply(BigDecimal.valueOf(dto.getCantidad())));

            subtotalGeneral = subtotalGeneral.add(det.getSubtotal());
            detallesList.add(det);
        }

        oc.setDetalles(detallesList);
        oc.setSubtotal(subtotalGeneral);
        oc.setImpuestos(subtotalGeneral.multiply(new BigDecimal("0.18"))); // IGV 18%
        oc.setTotal(oc.getSubtotal().add(oc.getImpuestos()));

        return ocRepository.save(oc);
    }

    public List<OrdenCompra> listar() {
        List<OrdenCompra> ordenes = ocRepository.findAll();
        ordenes.forEach(o -> {
            if (o.getProveedor() != null) o.getProveedor().getRazonSocial();
            if (o.getDetalles() != null) {
                o.getDetalles().size();
                o.getDetalles().forEach(d -> {
                    if (d.getRepuesto() != null) d.getRepuesto().getNombre();
                });
            }
        });
        return ordenes;
    }

    public OrdenCompra obtenerPorId(Long id) {
        return ocRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));
    }

    @Transactional
    public OrdenCompra cambiarEstado(Long id, String nuevoEstado) {
        OrdenCompra oc = obtenerPorId(id);
        if (nuevoEstado == null || nuevoEstado.isBlank()) {
            throw new RuntimeException("El estado es obligatorio");
        }
        switch (nuevoEstado) {
            case "APROBADA" -> {
                if (!"PENDIENTE".equals(oc.getEstado())) {
                    throw new RuntimeException("Solo se pueden aprobar órdenes pendientes");
                }
            }
            case "PENDIENTE" -> {
                if (!"PENDIENTE".equals(oc.getEstado())) {
                    throw new RuntimeException("Solo una orden pendiente puede mantenerse en ese estado");
                }
            }
            case "RECIBIDA" -> {
                if (!"APROBADA".equals(oc.getEstado()) && !"RECIBIDA".equals(oc.getEstado())) {
                    throw new RuntimeException("La orden debe estar aprobada antes de recibirla");
                }
            }
            case "CANCELADA" -> {
                if (!"PENDIENTE".equals(oc.getEstado()) && !"APROBADA".equals(oc.getEstado())) {
                    throw new RuntimeException("Solo se pueden cancelar órdenes pendientes o aprobadas");
                }
            }
            default -> throw new RuntimeException("Estado de orden no válido: " + nuevoEstado);
        }
        oc.setEstado(nuevoEstado);
        return ocRepository.save(oc);
    }

    @Transactional
    public OrdenCompra cancelar(Long id) {
        OrdenCompra oc = obtenerPorId(id);
        if (!"PENDIENTE".equals(oc.getEstado())) {
            throw new RuntimeException("Solo se pueden cancelar órdenes pendientes");
        }
        oc.setEstado("CANCELADA");
        return ocRepository.save(oc);
    }
}