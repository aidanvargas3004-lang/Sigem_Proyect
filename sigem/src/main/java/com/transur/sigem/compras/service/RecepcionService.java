package com.transur.sigem.compras.service;

import com.transur.sigem.compras.dto.RecepcionDetalleDTO;
import com.transur.sigem.compras.dto.RecepcionRequest;
import com.transur.sigem.compras.entity.OrdenCompra;
import com.transur.sigem.compras.entity.RecepcionCompra;
import com.transur.sigem.compras.entity.RecepcionDetalle;
import com.transur.sigem.compras.repository.OrdenCompraRepository;
import com.transur.sigem.compras.repository.RecepcionCompraRepository;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RecepcionService {

    private final RecepcionCompraRepository recepcionRepository;
    private final OrdenCompraRepository ocRepository;
    private final RepuestoRepository repuestoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public RecepcionCompra crear(RecepcionRequest request) {
        if (recepcionRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException("Ya existe una recepción con ese número");
        }

        OrdenCompra oc = ocRepository.findById(request.getOrdenCompraId())
                .orElseThrow(() -> new RuntimeException("Orden de compra no encontrada"));

        RecepcionCompra rec = new RecepcionCompra();
        rec.setOrdenCompra(oc);
        rec.setNumero(request.getNumero());
        rec.setFechaRecepcion(request.getFechaRecepcion() != null ? request.getFechaRecepcion() : LocalDate.now());
        rec.setObservaciones(request.getObservaciones());
        rec.setEstado("RECIBIDA");

        if (request.getUsuarioId() != null) {
            Usuario u = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            rec.setUsuario(u);
        }

        List<RecepcionDetalle> detallesList = new ArrayList<>();
        for (RecepcionDetalleDTO dto : request.getDetalles()) {
            RecepcionDetalle det = new RecepcionDetalle();
            det.setRecepcion(rec);

            if (dto.getRepuestoId() != null) {
                Repuesto r = repuestoRepository.findById(dto.getRepuestoId())
                        .orElseThrow(() -> new RuntimeException("Repuesto no encontrado"));
                det.setRepuesto(r);
                det.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion() : r.getNombre());
            } else {
                det.setDescripcion(dto.getDescripcion());
            }

            det.setCantidadRecibida(dto.getCantidadRecibida());
            det.setObservacion(dto.getObservacion());

            // La recepción actualiza el stock del repuesto catalogado.
            // Los ítems no catalogados se conservan en la recepción, pero no alteran stock.
            if (det.getRepuesto() != null) {
                Repuesto repuesto = det.getRepuesto();
                repuesto.setStockActual(repuesto.getStockActual() + det.getCantidadRecibida());
                repuestoRepository.save(repuesto);
            }

            detallesList.add(det);
        }

        rec.setDetalles(detallesList);

        // Opcional: Actualizar estado de la OC a RECIBIDA
        oc.setEstado("RECIBIDA");
        ocRepository.save(oc);

        return recepcionRepository.save(rec);
    }

    public List<RecepcionCompra> listar() {
        List<RecepcionCompra> recepciones = recepcionRepository.findAll();
        recepciones.forEach(r -> {
            if (r.getOrdenCompra() != null && r.getOrdenCompra().getProveedor() != null) {
                r.getOrdenCompra().getProveedor().getRazonSocial();
            }
            if (r.getDetalles() != null) {
                r.getDetalles().size();
                r.getDetalles().forEach(d -> {
                    if (d.getRepuesto() != null) d.getRepuesto().getNombre();
                });
            }
        });
        return recepciones;
    }

    public RecepcionCompra obtenerPorId(Long id) {
        return recepcionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recepción no encontrada"));
    }

    public List<RecepcionCompra> listarPorOrden(Long ordenCompraId) {
        // Agrega este método en RecepcionCompraRepository:
        // List<RecepcionCompra> findByOrdenCompraId(Long ordenCompraId);
        return recepcionRepository.findByOrdenCompraId(ordenCompraId);
    }
}