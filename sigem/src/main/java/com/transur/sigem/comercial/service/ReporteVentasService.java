package com.transur.sigem.comercial.service;

import com.transur.sigem.comercial.dto.ContratoResponse;
import com.transur.sigem.comercial.dto.ReporteVentasDTO;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.comercial.repository.CotizacionRepository;
import com.transur.sigem.comercial.repository.SolicitudRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReporteVentasService {
    private final ContratoRepository contratoRepository;
    private final CotizacionRepository cotizacionRepository;
    private final SolicitudRepository solicitudRepository;

    public ReporteVentasDTO generar(LocalDate desde, LocalDate hasta, String tipoContrato, String estado) {
        LocalDate fechaDesde = desde == null ? LocalDate.now().withDayOfMonth(1) : desde;
        LocalDate fechaHasta = hasta == null ? LocalDate.now() : hasta;
        if (fechaHasta.isBefore(fechaDesde)) {
            throw new IllegalArgumentException("La fecha hasta no puede ser anterior a la fecha desde");
        }

        String tipo = normalizarFiltro(tipoContrato);
        String est = normalizarFiltro(estado);

        List<Contrato> todos = contratoRepository.findAllByOrderByFechaInicioDesc();
        List<Contrato> filtrados = todos.stream()
                .filter(c -> c.getFechaInicio() != null && !c.getFechaInicio().isBefore(fechaDesde) && !c.getFechaInicio().isAfter(fechaHasta))
                .filter(c -> tipo == null || tipo.equals(c.getTipoContrato()))
                .filter(c -> est == null || est.equals(c.getEstado()))
                .toList();

        BigDecimal montoTotal = suma(filtrados);
        BigDecimal montoVigente = sumaPorEstado(filtrados, "VIGENTE");
        BigDecimal montoFinalizado = sumaPorEstado(filtrados, "FINALIZADO");

        long venta = contarTipo(filtrados, "VENTA");
        long alquiler = contarTipo(filtrados, "ALQUILER");
        long servicio = contarTipo(filtrados, "SERVICIO");

        return ReporteVentasDTO.builder()
                .desde(fechaDesde).hasta(fechaHasta).tipoContrato(tipo).estado(est)
                .totalSolicitudes(solicitudRepository.count())
                .solicitudesPendientes(solicitudRepository.findByEstado("PENDIENTE").size())
                .totalCotizaciones(cotizacionRepository.count())
                .cotizacionesAprobadas(cotizacionRepository.findAll().stream().filter(c -> "APROBADA".equals(c.getEstado())).count())
                .totalContratos(filtrados.size())
                .contratosVigentes(contarEstado(filtrados, "VIGENTE"))
                .contratosFinalizados(contarEstado(filtrados, "FINALIZADO"))
                .montoContratado(montoTotal)
                .montoVigente(montoVigente)
                .montoFinalizado(montoFinalizado)
                .contratosVenta(venta)
                .contratosAlquiler(alquiler)
                .contratosServicio(servicio)
                .montoVenta(sumaPorTipo(filtrados, "VENTA"))
                .montoAlquiler(sumaPorTipo(filtrados, "ALQUILER"))
                .montoServicio(sumaPorTipo(filtrados, "SERVICIO"))
                .contratos(filtrados.stream().map(this::map).toList())
                .build();
    }

    private String normalizarFiltro(String valor) {
        if (valor == null || valor.isBlank() || "TODOS".equalsIgnoreCase(valor)) return null;
        return valor.trim().toUpperCase();
    }

    private long contarTipo(List<Contrato> lista, String tipo) { return lista.stream().filter(c -> tipo.equals(c.getTipoContrato())).count(); }
    private long contarEstado(List<Contrato> lista, String estado) { return lista.stream().filter(c -> estado.equals(c.getEstado())).count(); }

    private BigDecimal suma(List<Contrato> lista) {
        return lista.stream().map(c -> c.getMontoTotal() == null ? BigDecimal.ZERO : c.getMontoTotal()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    private BigDecimal sumaPorEstado(List<Contrato> lista, String estado) { return suma(lista.stream().filter(c -> estado.equals(c.getEstado())).toList()); }
    private BigDecimal sumaPorTipo(List<Contrato> lista, String tipo) { return suma(lista.stream().filter(c -> tipo.equals(c.getTipoContrato())).toList()); }

    private ContratoResponse map(Contrato c) {
        return ContratoResponse.builder()
                .id(c.getId()).numero(c.getNumero())
                .clienteNombre(c.getCliente() == null ? "Sin cliente" : c.getCliente().getRazonSocial())
                .tipoContrato(c.getTipoContrato()).montoTotal(c.getMontoTotal())
                .fechaInicio(c.getFechaInicio()).fechaFin(c.getFechaFin()).estado(c.getEstado())
                .build();
    }
}
