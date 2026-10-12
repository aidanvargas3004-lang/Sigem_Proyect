package com.transur.sigem.operaciones.service;

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

    @Transactional
    public AsignacionResponse crear(AsignacionRequest request) {
        Servicio servicio = servicioRepository.findById(request.getServicioId())
                .orElseThrow(() -> new RuntimeException("Servicio no encontrado"));

        Maquinaria maquinaria = maquinariaRepository.findById(request.getMaquinariaId())
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

        if (!"POR_ASIGNAR".equals(servicio.getEstado()) && !"SUSPENDIDO".equals(servicio.getEstado())) {
            throw new RuntimeException("El servicio ya no está disponible para una nueva asignación");
        }
        if (!"OPERATIVO".equals(maquinaria.getEstadoOperativo())) {
            throw new RuntimeException("La maquinaria " + maquinaria.getCodigoInterno() + " no está disponible");
        }
        boolean maquinaOcupada = asignacionRepository.findByMaquinariaId(maquinaria.getId()).stream()
                .anyMatch(a -> "ACTIVA".equals(a.getEstado()));
        if (maquinaOcupada) throw new RuntimeException("La maquinaria ya tiene una asignación activa");

        Asignacion asignacion = new Asignacion();
        asignacion.setServicio(servicio);
        asignacion.setMaquinaria(maquinaria);

        if (request.getEmpleadoId() != null) {
            Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
            if (!Boolean.TRUE.equals(empleado.getEstado())) throw new RuntimeException("El empleado está inactivo");
            String cargo = empleado.getCargo() == null ? "" : empleado.getCargo().trim().toUpperCase();
            if (!List.of("CONDUCTOR", "OPERADOR").contains(cargo)) {
                throw new RuntimeException("El empleado seleccionado no está habilitado como operador/conductor");
            }
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
                .orElseThrow(() -> new RuntimeException("Asignación no encontrada"));

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