package com.transur.sigem.operaciones.service;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.operaciones.entity.Asignacion;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.operaciones.dto.ServicioRequest;
import com.transur.sigem.operaciones.dto.ServicioResponse;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.entity.TipoServicio;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.repository.TipoServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ServicioService {

    private final ServicioRepository servicioRepository;
    private final TipoServicioRepository tipoRepository;
    private final ContratoRepository contratoRepository;
    private final AsignacionRepository asignacionRepository;
    private final MaquinariaRepository maquinariaRepository;

    // ✅ CREAR
    @Transactional
    public ServicioResponse crear(ServicioRequest request) {
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Contrato", request.getContratoId()));

        TipoServicio tipo = tipoRepository.findById(request.getTipoServicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Tipo de servicio", request.getTipoServicioId()));

        Servicio servicio = new Servicio();
        servicio.setContrato(contrato);
        servicio.setTipoServicio(tipo);
        servicio.setOrigen(request.getOrigen());
        servicio.setDestino(request.getDestino());
        servicio.setFechaProgramada(request.getFechaProgramada());
        if (request.getFechaFinProgramada() != null && request.getFechaProgramada() != null
                && !request.getFechaFinProgramada().isAfter(request.getFechaProgramada())) {
            throw new NegocioException("La fecha de fin programada debe ser posterior al inicio");
        }
        servicio.setFechaFinProgramada(request.getFechaFinProgramada());
        servicio.setEstado(request.getEstado() != null ? request.getEstado() : "POR_ASIGNAR");
        servicio.setObservaciones(request.getObservaciones());

        return mapToResponse(servicioRepository.save(servicio));
    }

    // ✅ LISTAR TODOS
    public List<ServicioResponse> listar() {
        return servicioRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ OBTENER POR ID (EL QUE FALTABA)
    public ServicioResponse obtener(Long id) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
        return mapToResponse(servicio);
    }

    // ✅ ACTUALIZAR ESTADO (CRÍTICO PARA EL FLUJO OPERATIVO)
    @Transactional
    public ServicioResponse actualizarEstado(Long id, String nuevoEstado) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));

        // Validar transición de estados válida (opcional pero recomendado)
        String nuevo = nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase();
        if (!List.of("POR_ASIGNAR", "ASIGNADO", "EN_CURSO", "FINALIZADO", "SUSPENDIDO", "CANCELADO").contains(nuevo)) {
            throw new NegocioException("Estado de servicio no válido");
        }
        if (("EN_CURSO".equals(nuevo) || "FINALIZADO".equals(nuevo)) && !nuevo.equals(servicio.getEstado())) {
            // RF010: el inicio y el cierre exigen registrar las lecturas de la unidad
            throw new NegocioException("Use 'Registrar inicio' o 'Registrar cierre' para pasar el servicio a " + nuevo);
        }
        String actual = servicio.getEstado();
        if (("FINALIZADO".equals(actual) || "CANCELADO".equals(actual)) && !actual.equals(nuevo)) {
            throw new NegocioException("Un servicio finalizado/cancelado no puede reabrirse");
        }
        servicio.setEstado(nuevo);
        if ("FINALIZADO".equals(nuevo) || "CANCELADO".equals(nuevo)) {
            asignacionRepository.findByServicioId(servicio.getId()).stream()
                    .filter(a -> "ACTIVA".equals(a.getEstado()))
                    .forEach(a -> {
                        a.setEstado("COMPLETADA");
                        asignacionRepository.save(a);
                        liberarUnidad(a.getMaquinaria());
                    });
        }
        return mapToResponse(servicioRepository.save(servicio));
    }

    /** RF010: registra el inicio real con las lecturas iniciales de horómetro y kilometraje. */
    @Transactional
    public ServicioResponse iniciar(Long id, BigDecimal horometroInicial, BigDecimal kmInicial, LocalDateTime fechaHora) {
        Servicio s = buscar(id);
        if (!"ASIGNADO".equals(s.getEstado())) {
            throw new NegocioException("Solo se inician servicios ASIGNADOS (estado actual: " + s.getEstado() + ")");
        }
        Asignacion a = asignacionActiva(s);
        Maquinaria m = a.getMaquinaria();
        if (m.getEstadoOperativo() != null && m.getEstadoOperativo().startsWith("EN_MANTENIMIENTO")) {
            throw new NegocioException("La unidad " + m.getCodigoInterno() + " está en mantenimiento");
        }
        BigDecimal h = horometroInicial != null ? horometroInicial : nvl(m.getHorometroActual());
        BigDecimal k = kmInicial != null ? kmInicial : nvl(m.getKilometrajeActual());
        if (h.compareTo(nvl(m.getHorometroActual())) < 0 || k.compareTo(nvl(m.getKilometrajeActual())) < 0) {
            throw new NegocioException("Las lecturas iniciales no pueden ser menores que las actuales de la unidad ("
                    + nvl(m.getHorometroActual()) + " h / " + nvl(m.getKilometrajeActual()) + " km)");
        }
        s.setHorometroInicial(h);
        s.setKmInicial(k);
        s.setFechaInicioReal(fechaHora != null ? fechaHora : LocalDateTime.now());
        s.setEstado("EN_CURSO");
        m.setEstadoOperativo("EN_SERVICIO");
        return mapToResponse(s);
    }

    /** RF010: cierre con lecturas finales; calcula horas y km de uso y actualiza la unidad. */
    @Transactional
    public ServicioResponse finalizar(Long id, BigDecimal horometroFinal, BigDecimal kmFinal, LocalDateTime fechaHora,
                                     String observaciones) {
        Servicio s = buscar(id);
        if (!"EN_CURSO".equals(s.getEstado())) {
            throw new NegocioException("Solo se finalizan servicios EN CURSO");
        }
        if (horometroFinal == null) {
            throw new NegocioException("Registre el horómetro final de la unidad");
        }
        BigDecimal kmFin = kmFinal != null ? kmFinal : s.getKmInicial();
        if (horometroFinal.compareTo(nvl(s.getHorometroInicial())) < 0 || nvl(kmFin).compareTo(nvl(s.getKmInicial())) < 0) {
            throw new NegocioException("Las lecturas finales no pueden ser menores que las iniciales ("
                    + nvl(s.getHorometroInicial()) + " h / " + nvl(s.getKmInicial()) + " km)");
        }
        LocalDateTime fin = fechaHora != null ? fechaHora : LocalDateTime.now();
        if (s.getFechaInicioReal() != null && !fin.isAfter(s.getFechaInicioReal())) {
            throw new NegocioException("La hora de cierre debe ser posterior al inicio real");
        }
        s.setHorometroFinal(horometroFinal);
        s.setKmFinal(kmFin);
        s.setHorasUso(horometroFinal.subtract(nvl(s.getHorometroInicial())));
        s.setKmRecorridos(nvl(kmFin).subtract(nvl(s.getKmInicial())));
        s.setFechaFinReal(fin);
        if (observaciones != null && !observaciones.isBlank()) {
            s.setObservaciones((s.getObservaciones() != null ? s.getObservaciones() + "\n" : "") + "Cierre: " + observaciones.trim());
        }
        s.setEstado("FINALIZADO");
        for (Asignacion a : asignacionRepository.findByServicioId(s.getId())) {
            if (!"ACTIVA".equals(a.getEstado())) {
                continue;
            }
            Maquinaria m = a.getMaquinaria();
            m.setHorometroActual(horometroFinal);
            m.setKilometrajeActual(nvl(kmFin));
            a.setEstado("COMPLETADA");
            asignacionRepository.save(a);
            liberarUnidad(m);
        }
        return mapToResponse(s);
    }

    /** La unidad vuelve a OPERATIVO solo si ya no tiene otras asignaciones activas. */
    private void liberarUnidad(Maquinaria m) {
        if (m == null || ValidadorDisponibilidad.ESTADOS_NO_ASIGNABLES.contains(m.getEstadoOperativo())
                || (m.getEstadoOperativo() != null && m.getEstadoOperativo().startsWith("EN_MANTENIMIENTO"))) {
            return;
        }
        boolean ocupada = !asignacionRepository.findByMaquinariaIdAndEstado(m.getId(), "ACTIVA").isEmpty();
        m.setEstadoOperativo(ocupada ? "EN_SERVICIO" : "OPERATIVO");
        maquinariaRepository.save(m);
    }

    private Servicio buscar(Long id) {
        return servicioRepository.findById(id).orElseThrow(() -> new RecursoNoEncontradoException("Servicio", id));
    }

    private Asignacion asignacionActiva(Servicio s) {
        return asignacionRepository.findByServicioId(s.getId()).stream()
                .filter(a -> "ACTIVA".equals(a.getEstado())).findFirst()
                .orElseThrow(() -> new NegocioException("El servicio SER-" + s.getId() + " no tiene una unidad asignada"));
    }

    private static BigDecimal nvl(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }

    // ✅ LISTAR POR CONTRATO (ÚTIL PARA VER SERVICIOS DE UN CONTRATO ESPECÍFICO)
    public List<ServicioResponse> listarPorContrato(Long contratoId) {
        // Necesitas agregar este método en ServicioRepository:
        // List<Servicio> findByContratoId(Long contratoId);
        return servicioRepository.findByContratoId(contratoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ MAPPER PRIVADO
    private ServicioResponse mapToResponse(Servicio s) {
        return ServicioResponse.builder()
                .id(s.getId())
                .numeroContrato(s.getContrato().getNumero())
                .tipoServicioNombre(s.getTipoServicio().getNombre())
                .origen(s.getOrigen())
                .destino(s.getDestino())
                .fechaProgramada(s.getFechaProgramada())
                .estado(s.getEstado())
                .observaciones(s.getObservaciones())
                .build();
    }
}