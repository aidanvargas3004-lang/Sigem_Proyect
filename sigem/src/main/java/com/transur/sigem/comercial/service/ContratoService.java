package com.transur.sigem.comercial.service;

import com.transur.sigem.comercial.dto.ContratoRequest;
import com.transur.sigem.comercial.dto.ContratoResponse;
import com.transur.sigem.comercial.dto.ContratoDetalleResponse;
import com.transur.sigem.comercial.dto.ContratoLineaResponse;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.entity.Cotizacion;
import com.transur.sigem.comercial.entity.CotizacionDetalle;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.comercial.repository.CotizacionRepository;
import com.transur.sigem.comercial.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ContratoService {

    private final ContratoRepository contratoRepository;
    private final CotizacionRepository cotizacionRepository;
    private final SolicitudRepository solicitudRepository;

    public List<ContratoResponse> listar() {
        return contratoRepository.findAllByOrderByFechaInicioDesc().stream()
                .map(this::mapToResponse)
                .toList();
    }

    public Contrato obtenerEntidad(Long id) {
        return contratoRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Contrato no encontrado"));
    }

    public ContratoResponse obtener(Long id) {
        return mapToResponse(obtenerEntidad(id));
    }

    @Transactional(readOnly = true)
    public ContratoDetalleResponse obtenerDetalle(Long id) {
        Contrato c = obtenerEntidad(id);
        Cotizacion cot = c.getCotizacion();
        List<ContratoLineaResponse> lineas = cot.getDetalles() == null ? List.of() : cot.getDetalles().stream()
                .map(this::mapLinea)
                .toList();
        return ContratoDetalleResponse.builder()
                .id(c.getId()).numero(c.getNumero())
                .clienteNombre(c.getCliente() == null ? "Sin cliente" : c.getCliente().getRazonSocial())
                .clienteDocumento(c.getCliente() == null ? "-" : c.getCliente().getDocumento())
                .tipoContrato(c.getTipoContrato()).estado(c.getEstado())
                .fechaInicio(c.getFechaInicio()).fechaFin(c.getFechaFin()).montoTotal(c.getMontoTotal())
                .cotizacionId(cot == null ? null : cot.getId())
                .cotizacionNumero(cot == null ? "-" : cot.getNumero())
                .cotizacionEstado(cot == null ? "-" : cot.getEstado())
                .cotizacionSubtotal(cot == null ? BigDecimal.ZERO : cot.getSubtotal())
                .cotizacionImpuestos(cot == null ? BigDecimal.ZERO : cot.getImpuestos())
                .cotizacionTotal(cot == null ? BigDecimal.ZERO : cot.getTotal())
                .solicitudDescripcion(cot == null || cot.getSolicitud() == null ? "-" : cot.getSolicitud().getDescripcion())
                .lineas(lineas)
                .build();
    }

    private ContratoLineaResponse mapLinea(CotizacionDetalle d) {
        String maquinaria = d.getMaquinaria() == null ? "-" : d.getMaquinaria().getCodigoInterno() + " - " + d.getMaquinaria().getMarca() + " " + d.getMaquinaria().getModelo();
        return ContratoLineaResponse.builder()
                .maquinaria(maquinaria).descripcion(d.getDescripcion())
                .cantidad(d.getCantidad()).precioUnitario(d.getPrecioUnitario()).subtotal(d.getSubtotal()).build();
    }

    public List<Cotizacion> cotizacionesAprobadasDisponibles() {
        return cotizacionRepository.findAll().stream()
                .filter(c -> "APROBADA".equals(c.getEstado()))
                .filter(c -> !contratoRepository.existsByCotizacionId(c.getId()))
                .toList();
    }

    @Transactional
    public ContratoResponse crear(ContratoRequest request) {
        Cotizacion cotizacion = cotizacionRepository.findById(request.getCotizacionId())
                .orElseThrow(() -> new IllegalArgumentException("Cotización no encontrada"));

        if (!"APROBADA".equals(cotizacion.getEstado())) {
            throw new IllegalArgumentException("Solo se pueden crear contratos desde cotizaciones APROBADAS por el cliente");
        }
        if (cotizacion.getSolicitud() == null) {
            throw new IllegalArgumentException("La cotización no tiene una solicitud vinculada");
        }
        if (!"APROBADA".equals(cotizacion.getSolicitud().getEstado())) {
            throw new IllegalArgumentException("La solicitud vinculada debe estar APROBADA por el cliente");
        }
        if (cotizacion.getCliente() == null || cotizacion.getSolicitud().getCliente() == null
                || !cotizacion.getCliente().getId().equals(cotizacion.getSolicitud().getCliente().getId())) {
            throw new IllegalArgumentException("La cotización y la solicitud no pertenecen al mismo cliente");
        }
        if (contratoRepository.existsByCotizacionId(cotizacion.getId())) {
            throw new IllegalArgumentException("La cotización ya tiene un contrato asociado");
        }

        String tipo = normalizarTipo(request.getTipoContrato());
        String tipoSolicitud = cotizacion.getSolicitud().getTipoSolicitud();
        if (!tipo.equals(tipoSolicitud)) {
            throw new IllegalArgumentException("El tipo de contrato debe coincidir con el tipo de la solicitud: " + tipoSolicitud);
        }

        LocalDate inicio = request.getFechaInicio() == null ? LocalDate.now() : request.getFechaInicio();
        LocalDate fin = request.getFechaFin();
        if (fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio");
        }
        if ("VENTA".equals(tipo)) {
            fin = null;
        }

        String numero = request.getNumero();
        if (numero == null || numero.isBlank()) {
            numero = generarNumero();
        } else {
            numero = numero.trim();
        }
        if (contratoRepository.existsByNumero(numero)) {
            throw new IllegalArgumentException("Ya existe un contrato con número: " + numero);
        }

        Contrato contrato = new Contrato();
        contrato.setCliente(cotizacion.getCliente());
        contrato.setCotizacion(cotizacion);
        contrato.setNumero(numero);
        contrato.setTipoContrato(tipo);
        contrato.setFechaInicio(inicio);
        contrato.setFechaFin(fin);
        contrato.setMontoTotal(cotizacion.getTotal());
        contrato.setEstado("VIGENTE");

        return mapToResponse(contratoRepository.save(contrato));
    }

    @Transactional
    public ContratoResponse actualizar(Long id, ContratoRequest request) {
        Contrato contrato = obtenerEntidad(id);

        if ("FINALIZADO".equals(contrato.getEstado()) || "CANCELADO".equals(contrato.getEstado())) {
            throw new IllegalArgumentException("No se puede editar un contrato finalizado o cancelado");
        }

        String tipo = normalizarTipo(request.getTipoContrato());
        if (!tipo.equals(contrato.getCotizacion().getSolicitud().getTipoSolicitud())) {
            throw new IllegalArgumentException("El tipo de contrato debe coincidir con la solicitud vinculada");
        }

        if (request.getNumero() != null && !request.getNumero().isBlank()
                && !request.getNumero().equals(contrato.getNumero())
                && contratoRepository.existsByNumero(request.getNumero().trim())) {
            throw new IllegalArgumentException("Ya existe un contrato con número: " + request.getNumero());
        }

        LocalDate inicio = request.getFechaInicio();
        LocalDate fin = request.getFechaFin();
        if (inicio == null) {
            throw new IllegalArgumentException("La fecha de inicio es obligatoria");
        }
        if (fin != null && fin.isBefore(inicio)) {
            throw new IllegalArgumentException("La fecha fin no puede ser anterior a la fecha inicio");
        }
        if ("VENTA".equals(tipo)) fin = null;

        contrato.setNumero(request.getNumero() == null || request.getNumero().isBlank()
                ? contrato.getNumero() : request.getNumero().trim());
        contrato.setTipoContrato(tipo);
        contrato.setFechaInicio(inicio);
        contrato.setFechaFin(fin);

        return mapToResponse(contratoRepository.save(contrato));
    }

    @Transactional
    public ContratoResponse cambiarEstado(Long id, String nuevoEstado) {
        Contrato contrato = obtenerEntidad(id);
        String nuevo = nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase();
        if (!List.of("VIGENTE", "SUSPENDIDO", "FINALIZADO", "CANCELADO").contains(nuevo)) {
            throw new IllegalArgumentException("Estado de contrato no válido");
        }

        String actual = contrato.getEstado();
        if (("FINALIZADO".equals(actual) || "CANCELADO".equals(actual)) && !actual.equals(nuevo)) {
            throw new IllegalArgumentException("Un contrato " + actual.toLowerCase() + " no puede volver a un estado activo");
        }
        if ("VIGENTE".equals(actual) && "VIGENTE".equals(nuevo)) {
            return mapToResponse(contrato);
        }

        contrato.setEstado(nuevo);
        Contrato guardado = contratoRepository.save(contrato);
        if (("FINALIZADO".equals(nuevo) || "CANCELADO".equals(nuevo))
                && contrato.getCotizacion() != null
                && contrato.getCotizacion().getSolicitud() != null) {
            contrato.getCotizacion().getSolicitud().setEstado("CERRADA");
            solicitudRepository.save(contrato.getCotizacion().getSolicitud());
        }
        return mapToResponse(guardado);
    }

    private String generarNumero() {
        int year = LocalDate.now().getYear();
        long count = contratoRepository.count() + 1;
        String numero;
        do {
            numero = String.format("CTR-%d-%03d", year, count++);
        } while (contratoRepository.existsByNumero(numero));
        return numero;
    }

    private String normalizarTipo(String tipo) {
        String valor = tipo == null ? "" : tipo.trim().toUpperCase();
        if (!List.of("VENTA", "ALQUILER", "SERVICIO").contains(valor)) {
            throw new IllegalArgumentException("Tipo de contrato no válido");
        }
        return valor;
    }

    private ContratoResponse mapToResponse(Contrato c) {
        return ContratoResponse.builder()
                .id(c.getId())
                .numero(c.getNumero())
                .cotizacionId(c.getCotizacion() == null ? null : c.getCotizacion().getId())
                .cotizacionNumero(c.getCotizacion() == null ? null : c.getCotizacion().getNumero())
                .clienteNombre(c.getCliente() == null ? "Sin cliente" : c.getCliente().getRazonSocial())
                .tipoContrato(c.getTipoContrato())
                .montoTotal(c.getMontoTotal())
                .fechaInicio(c.getFechaInicio())
                .fechaFin(c.getFechaFin())
                .estado(c.getEstado())
                .build();
    }
}
