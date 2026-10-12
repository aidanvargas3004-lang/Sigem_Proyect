package com.transur.sigem.operaciones.service;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.dto.AsignacionRequest;
import com.transur.sigem.operaciones.dto.AsignacionResponse;
import com.transur.sigem.operaciones.entity.Asignacion;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final ServicioRepository servicioRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final ValidadorDisponibilidad validador;

    @Transactional
    public AsignacionResponse crear(AsignacionRequest request) {
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", request.getServicioId()));

        Maquinaria maquinaria = maquinariaRepository.findById(request.getMaquinariaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Maquinaria", request.getMaquinariaId()));

        if (!"POR_ASIGNAR".equals(servicio.getEstado()) && !"SUSPENDIDO".equals(servicio.getEstado())) {
            throw new NegocioException("El servicio ya no está disponible para una nueva asignación");
        }
        // RF009: conflictos de unidad, operador, mantenimiento y licencia en el rango del servicio
        validador.validarUnidad(maquinaria, servicio);

        Asignacion asignacion = new Asignacion();
        asignacion.setServicio(servicio);
        asignacion.setMaquinaria(maquinaria);

        if (request.getEmpleadoId() != null) {
            Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Empleado", request.getEmpleadoId()));
            validador.validarOperador(empleado, servicio);
            asignacion.setEmpleado(empleado);
        }

        asignacion.setFechaAsignacion(request.getFechaAsignacion() != null ?
                request.getFechaAsignacion() : LocalDateTime.now());
        asignacion.setEstado(request.getEstado() != null ? request.getEstado() : "ACTIVA");

        maquinaria.setEstadoOperativo("EN_SERVICIO");
        maquinariaRepository.save(maquinaria);
        Asignacion guardada = asignacionRepository.save(asignacion);

        // Actualizar estado del servicio automáticamente
        servicio.setEstado("ASIGNADO");
        servicioRepository.save(servicio);

        return mapToResponse(guardada);
    }

    private AsignacionResponse mapToResponse(Asignacion a) {
        String nombreEmp = null;
        if (a.getEmpleado() != null) {
            // ✅ CORRECCIÓN: Concatenación segura de nombres
            nombreEmp = a.getEmpleado().getNombres() + " " + a.getEmpleado().getApellidos();
        }

        return AsignacionResponse.builder()
                .id(a.getId())
                .servicioId(a.getServicio().getId())
                .maquinariaCodigo(a.getMaquinaria().getCodigoInterno())
                .empleadoNombre(nombreEmp)
                .fechaAsignacion(a.getFechaAsignacion())
                .estado(a.getEstado())
                .build();
    }

    @Transactional
    public AsignacionResponse cancelar(Long id, String motivo) {
        Asignacion a = asignacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Asignación", id));

        a.setEstado("CANCELADA");
        if (a.getMaquinaria() != null && !"VENDIDO".equals(a.getMaquinaria().getEstadoOperativo())) {
            a.getMaquinaria().setEstadoOperativo("OPERATIVO");
            maquinariaRepository.save(a.getMaquinaria());
        }
        if (motivo != null) {
            // Podrías agregar un campo 'observaciones' a Asignacion para guardar el motivo
            // a.setObservaciones("Cancelada: " + motivo);
        }
        return mapToResponse(asignacionRepository.save(a));
    }

    public List<AsignacionResponse> listarPorEmpleado(Long empleadoId) {
        return asignacionRepository.findByEmpleadoIdAndEstado(empleadoId, "ACTIVA").stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<AsignacionResponse> listarPorMaquinaria(Long maquinariaId) {
        return asignacionRepository.findByMaquinariaId(maquinariaId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }
}