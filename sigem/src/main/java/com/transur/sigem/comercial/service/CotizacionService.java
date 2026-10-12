package com.transur.sigem.comercial.service;

import com.transur.sigem.comercial.dto.CotizacionDetalleDTO;
import com.transur.sigem.comercial.dto.CotizacionRequest;
import com.transur.sigem.comercial.dto.CotizacionResponse;
import com.transur.sigem.comercial.entity.Cotizacion;
import com.transur.sigem.comercial.entity.CotizacionDetalle;
import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.repository.CotizacionRepository;
import com.transur.sigem.comercial.repository.SolicitudRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.Tarifa;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
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
public class CotizacionService {
    private static final BigDecimal IGV = new BigDecimal("0.18");

    private final CotizacionRepository cotizacionRepository;
    private final SolicitudRepository solicitudRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final TarifaRepository tarifaRepository;

    @Transactional
    public CotizacionResponse crear(CotizacionRequest request) {
        Solicitud solicitud = solicitudRepository.findById(request.getSolicitudId())
                .orElseThrow(() -> new IllegalArgumentException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(solicitud.getEstado())) {
            throw new IllegalArgumentException("La solicitud no está disponible para una nueva cotización. Estado actual: " + solicitud.getEstado());
        }
        if (cotizacionRepository.existsBySolicitudId(solicitud.getId())) {
            throw new IllegalArgumentException("La solicitud ya tiene una cotización");
        }

        String numero = (request.getNumero() == null || request.getNumero().isBlank()) ? generarNumero() : request.getNumero().trim();
        if (cotizacionRepository.existsByNumero(numero)) {
            throw new IllegalArgumentException("Ya existe una cotización con número: " + numero);
        }

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new IllegalArgumentException("Debe agregar al menos un detalle");
        }

        Cotizacion cotizacion = new Cotizacion();
        cotizacion.setSolicitud(solicitud);
        cotizacion.setCliente(solicitud.getCliente());
        cotizacion.setNumero(numero);
        cotizacion.setFechaEmision(request.getFechaEmision() == null ? LocalDate.now() : request.getFechaEmision());
        cotizacion.setVigencia(request.getVigencia());
        cotizacion.setEstado("ENVIADA");

        List<CotizacionDetalle> detalles = new ArrayList<>();
        BigDecimal subtotal = BigDecimal.ZERO;

        String tipoSolicitud = solicitud.getTipoSolicitud();
        for (CotizacionDetalleDTO dto : request.getDetalles()) {
            if (dto.getCantidad() == null || dto.getCantidad() <= 0) throw new IllegalArgumentException("Cantidad inválida");

            Maquinaria maquinaria = null;
            Tarifa tarifa = null;
            if (dto.getMaquinariaId() != null) {
                maquinaria = maquinariaRepository.findById(dto.getMaquinariaId())
                        .orElseThrow(() -> new IllegalArgumentException("Maquinaria no encontrada: " + dto.getMaquinariaId()));
            }
            if (dto.getTarifaId() != null) {
                tarifa = tarifaRepository.findById(dto.getTarifaId())
                        .orElseThrow(() -> new IllegalArgumentException("Tarifa no encontrada: " + dto.getTarifaId()));
                if (maquinaria == null || tarifa.getMaquinaria() == null || !tarifa.getMaquinaria().getId().equals(maquinaria.getId())) {
                    throw new IllegalArgumentException("La tarifa seleccionada no corresponde a la maquinaria");
                }
            }

            if ("ALQUILER".equals(tipoSolicitud) && (maquinaria == null || tarifa == null)) {
                throw new IllegalArgumentException("Un alquiler requiere maquinaria y una tarifa vigente");
            }
            if ("VENTA".equals(tipoSolicitud) && maquinaria == null) {
                throw new IllegalArgumentException("Una venta requiere maquinaria");
            }

            BigDecimal precio = tarifa != null ? tarifa.getPrecio() : dto.getPrecioUnitario();
            if (precio == null || precio.signum() <= 0) {
                throw new IllegalArgumentException("El precio unitario debe ser mayor a 0");
            }
            BigDecimal linea = precio.multiply(BigDecimal.valueOf(dto.getCantidad()));

            CotizacionDetalle detalle = new CotizacionDetalle();
            detalle.setCotizacion(cotizacion);
            detalle.setMaquinaria(maquinaria);
            String descripcion = dto.getDescripcion();
            if (descripcion == null || descripcion.isBlank()) {
                descripcion = maquinaria != null
                        ? maquinaria.getCodigoInterno() + " - " + maquinaria.getMarca() + " " + maquinaria.getModelo() + (tarifa != null ? " / " + tarifa.getTipoTarifa() : "")
                        : "Servicio contratado";
            }
            detalle.setDescripcion(descripcion.trim());
            detalle.setCantidad(dto.getCantidad());
            detalle.setPrecioUnitario(precio);
            detalle.setSubtotal(linea);
            detalles.add(detalle);
            subtotal = subtotal.add(linea);
        }

        cotizacion.setDetalles(detalles);
        cotizacion.setSubtotal(subtotal);
        cotizacion.setImpuestos(subtotal.multiply(IGV));
        cotizacion.setTotal(subtotal.add(cotizacion.getImpuestos()));

        solicitud.setEstado("EN_COTIZACION");
        solicitudRepository.save(solicitud);
        return mapToResponse(cotizacionRepository.save(cotizacion));
    }

    public List<CotizacionResponse> listar() {
        return cotizacionRepository.findAll().stream().map(this::mapToResponse).toList();
    }

    @Transactional
    public void cambiarEstado(Long id, String nuevoEstado) {
        Cotizacion cot = cotizacionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Cotización no encontrada"));
        String actual = cot.getEstado();
        String nuevo = nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase();

        if (!List.of("RECHAZADA", "EXPIRADA").contains(nuevo)) {
            throw new IllegalArgumentException("Desde Ventas solo se permite marcar la cotización como rechazada o expirada; la aprobación corresponde al cliente");
        }
        if (!"ENVIADA".equals(actual)) {
            throw new IllegalArgumentException("La cotización debe estar ENVIADA para cambiar su estado");
        }
        if ("EXPIRADA".equals(nuevo) && cot.getVigencia() != null && !cot.getVigencia().isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("La cotización todavía está vigente");
        }

        cot.setEstado(nuevo);
        if (cot.getSolicitud() != null) {
            cot.getSolicitud().setEstado(nuevo);
            solicitudRepository.save(cot.getSolicitud());
        }
        cotizacionRepository.save(cot);
    }

    private String generarNumero() {
        int year = LocalDate.now().getYear();
        long count = cotizacionRepository.count() + 1;
        String numero;
        do {
            numero = String.format("COT-%d-%03d", year, count++);
        } while (cotizacionRepository.existsByNumero(numero));
        return numero;
    }

    private CotizacionResponse mapToResponse(Cotizacion c) {
        return CotizacionResponse.builder()
                .id(c.getId()).numero(c.getNumero())
                .solicitudId(c.getSolicitud() == null ? null : c.getSolicitud().getId())
                .clienteNombre(c.getCliente() == null ? "Sin cliente" : c.getCliente().getRazonSocial())
                .tipoSolicitud(c.getSolicitud() == null ? "-" : c.getSolicitud().getTipoSolicitud())
                .total(c.getTotal()).fechaEmision(c.getFechaEmision()).vigencia(c.getVigencia()).estado(c.getEstado())
                .build();
    }
}
