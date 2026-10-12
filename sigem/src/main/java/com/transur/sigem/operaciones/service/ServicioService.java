package com.transur.sigem.operaciones.service;

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
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado con ID: " + request.getContratoId()));

        TipoServicio tipo = tipoRepository.findById(request.getTipoServicioId())
                .orElseThrow(() -> new RuntimeException("Tipo de servicio no encontrado"));

        Servicio servicio = new Servicio();
        servicio.setContrato(contrato);
        servicio.setTipoServicio(tipo);
        servicio.setOrigen(request.getOrigen());
        servicio.setDestino(request.getDestino());
        servicio.setFechaProgramada(request.getFechaProgramada());
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
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado con ID: " + id));
        return mapToResponse(servicio);
    }

    // ✅ ACTUALIZAR ESTADO (CRÍTICO PARA EL FLUJO OPERATIVO)
    @Transactional
    public ServicioResponse actualizarEstado(Long id, String nuevoEstado) {
        Servicio servicio = servicioRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        // Validar transición de estados válida (opcional pero recomendado)
        String nuevo = nuevoEstado == null ? "" : nuevoEstado.trim().toUpperCase();
        if (!List.of("POR_ASIGNAR", "ASIGNADO", "EN_CURSO", "FINALIZADO", "SUSPENDIDO", "CANCELADO").contains(nuevo)) {
            throw new RuntimeException("Estado de servicio no válido");
        }
        String actual = servicio.getEstado();
        if (("FINALIZADO".equals(actual) || "CANCELADO".equals(actual)) && !actual.equals(nuevo)) {
            throw new RuntimeException("Un servicio finalizado/cancelado no puede reabrirse");
        }
        servicio.setEstado(nuevo);
        if ("FINALIZADO".equals(nuevo) || "CANCELADO".equals(nuevo)) {
            asignacionRepository.findByServicioId(servicio.getId()).stream()
                    .filter(a -> "ACTIVA".equals(a.getEstado()))
                    .forEach(a -> {
                        a.setEstado("COMPLETADA");
                        if (a.getMaquinaria() != null && !"VENDIDO".equals(a.getMaquinaria().getEstadoOperativo())) {
                            a.getMaquinaria().setEstadoOperativo("OPERATIVO");
                            maquinariaRepository.save(a.getMaquinaria());
                        }
                        asignacionRepository.save(a);
                    });
        }
        return mapToResponse(servicioRepository.save(servicio));
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