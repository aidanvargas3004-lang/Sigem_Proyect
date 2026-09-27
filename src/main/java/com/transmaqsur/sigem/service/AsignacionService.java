package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoAsignacion;
import com.transmaqsur.sigem.model.enums.EstadoEmpleado;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.OrigenAsignacion;
import com.transmaqsur.sigem.model.enums.TipoNotificacion;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.AsignacionRepository;
import com.transmaqsur.sigem.repository.OrdenMantenimientoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Asignación de unidades y personal. Centraliza la validación que impide
 * asignar la misma unidad o el mismo conductor/operador a dos trabajos que se
 * cruzan en el tiempo, o una unidad con mantenimiento programado.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AsignacionService {

    static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AsignacionRepository asignacionRepository;
    private final OrdenMantenimientoRepository ordenRepository;
    private final UnidadService unidadService;
    private final EmpleadoService empleadoService;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<Asignacion> listar() {
        return asignacionRepository.findAllByOrderByFechaInicioDesc();
    }

    @Transactional(readOnly = true)
    public List<Asignacion> listarActivas() {
        return asignacionRepository.findByEstadoInOrderByFechaInicioAsc(EstadosActivos.ASIGNACION);
    }

    @Transactional(readOnly = true)
    public List<Asignacion> deUnidad(Long unidadId) {
        return asignacionRepository.findByUnidadIdOrderByFechaInicioDesc(unidadId);
    }

    @Transactional(readOnly = true)
    public List<Asignacion> deEmpleado(Long empleadoId) {
        return asignacionRepository.findByEmpleadoIdOrderByFechaInicioDesc(empleadoId);
    }

    @Transactional(readOnly = true)
    public List<Asignacion> activasDeEmpleado(Long empleadoId) {
        return asignacionRepository.findByEmpleadoIdAndEstadoInOrderByFechaInicioAsc(empleadoId, EstadosActivos.ASIGNACION);
    }

    @Transactional(readOnly = true)
    public Asignacion obtener(Long id) {
        return asignacionRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Asignación", id));
    }

    /**
     * Valida y completa la asignación con las entidades administradas.
     *
     * @param excluirId id de la propia asignación cuando se está editando (0 si es nueva)
     */
    public void validar(Asignacion a, Long excluirId) {
        if (a.getUnidad() == null || a.getFechaInicio() == null || a.getFechaFin() == null) {
            throw new NegocioException("Seleccione la unidad e indique las fechas de inicio y fin");
        }
        if (!a.getFechaFin().isAfter(a.getFechaInicio())) {
            throw new NegocioException("La fecha y hora de fin debe ser posterior a la de inicio");
        }
        Unidad u = unidadService.obtener(a.getUnidad().getId());
        a.setUnidad(u);
        if (u.getEstado() == EstadoUnidad.FUERA_SERVICIO) {
            throw new NegocioException("La unidad " + u.getCodigo() + " está fuera de servicio");
        }
        List<Asignacion> cruces = asignacionRepository.cruceUnidad(u.getId(), a.getFechaInicio(), a.getFechaFin(),
                EstadosActivos.ASIGNACION, excluirId);
        if (!cruces.isEmpty()) {
            Asignacion c = cruces.get(0);
            throw new NegocioException("La unidad " + u.getCodigo() + " ya está asignada del " + FMT.format(c.getFechaInicio())
                    + " al " + FMT.format(c.getFechaFin()) + " (" + nvl(c.getReferencia(), c.getOrigen().getLabel())
                    + "). No se permite la doble asignación.");
        }
        List<OrdenMantenimiento> ots = ordenRepository.programadasEnRango(u.getId(), a.getFechaInicio().toLocalDate(),
                a.getFechaFin().toLocalDate(), EstadosActivos.ORDEN);
        if (!ots.isEmpty()) {
            OrdenMantenimiento ot = ots.get(0);
            throw new NegocioException("La unidad " + u.getCodigo() + " tiene el mantenimiento " + ot.getCodigo()
                    + " programado para el " + ot.getFechaProgramada() + ". Reprograme el mantenimiento o elija otra unidad.");
        }
        if (a.getEmpleado() != null) {
            Empleado e = empleadoService.obtener(a.getEmpleado().getId());
            a.setEmpleado(e);
            if (e.getEstado() != EstadoEmpleado.ACTIVO) {
                throw new NegocioException(e.getNombreCompleto() + " no está disponible (" + e.getEstado().getLabel() + ")");
            }
            if (!e.isPersonalDeCampo()) {
                throw new NegocioException("Solo se puede asignar conductores u operadores a una unidad");
            }
            if (e.getLicenciaVencimiento() != null && e.getLicenciaVencimiento().isBefore(a.getFechaFin().toLocalDate())) {
                throw new NegocioException("La licencia de " + e.getNombreCompleto() + " vence el " + e.getLicenciaVencimiento()
                        + ", antes de terminar la asignación");
            }
            List<Asignacion> crucesEmp = asignacionRepository.cruceEmpleado(e.getId(), a.getFechaInicio(), a.getFechaFin(),
                    EstadosActivos.ASIGNACION, excluirId);
            if (!crucesEmp.isEmpty()) {
                Asignacion c = crucesEmp.get(0);
                throw new NegocioException(e.getNombreCompleto() + " ya está asignado a " + c.getUnidad().getCodigo() + " del "
                        + FMT.format(c.getFechaInicio()) + " al " + FMT.format(c.getFechaFin())
                        + " (" + nvl(c.getReferencia(), c.getOrigen().getLabel()) + ")");
            }
        }
    }

    // ------------------------------------------------------------ Asignaciones internas

    public Asignacion crearInterna(Asignacion form) {
        form.setOrigen(OrigenAsignacion.INTERNA);
        form.setEstado(EstadoAsignacion.PROGRAMADA);
        validar(form, 0L);
        Asignacion a = asignacionRepository.save(form);
        a.setReferencia("ASG-" + String.format("%05d", a.getId()));
        unidadService.recalcularEstado(a.getUnidad());
        notificarAsignacion(a);
        return a;
    }

    public void iniciarInterna(Long id) {
        Asignacion a = obtenerInterna(id);
        if (a.getEstado() != EstadoAsignacion.PROGRAMADA) {
            throw new NegocioException("La asignación no está programada");
        }
        exigirUnidadLibre(a.getUnidad());
        a.setEstado(EstadoAsignacion.EN_CURSO);
        unidadService.recalcularEstado(a.getUnidad());
    }

    public void finalizarInterna(Long id) {
        Asignacion a = obtenerInterna(id);
        if (a.getEstado() != EstadoAsignacion.EN_CURSO) {
            throw new NegocioException("La asignación no está en curso");
        }
        a.setEstado(EstadoAsignacion.FINALIZADA);
        unidadService.recalcularEstado(a.getUnidad());
    }

    public void cancelarInterna(Long id) {
        Asignacion a = obtenerInterna(id);
        if (a.getEstado() != EstadoAsignacion.PROGRAMADA) {
            throw new NegocioException("Solo se cancelan asignaciones programadas");
        }
        a.setEstado(EstadoAsignacion.CANCELADA);
        unidadService.recalcularEstado(a.getUnidad());
    }

    private Asignacion obtenerInterna(Long id) {
        Asignacion a = obtener(id);
        if (a.getOrigen() != OrigenAsignacion.INTERNA) {
            throw new NegocioException("Esta asignación pertenece a " + a.getReferencia() + "; gestiónela desde ese documento");
        }
        return a;
    }

    // ------------------------------------------------------------ Apoyo a operaciones

    /** Verifica que la unidad pueda empezar a trabajar ahora mismo. */
    public void exigirUnidadLibre(Unidad u) {
        if (u.getEstado() == EstadoUnidad.EN_MANTENIMIENTO || u.getEstado() == EstadoUnidad.FUERA_SERVICIO) {
            throw new NegocioException("La unidad " + u.getCodigo() + " está " + u.getEstado().getLabel().toLowerCase());
        }
        if (asignacionRepository.countByUnidadIdAndEstadoIn(u.getId(), List.of(EstadoAsignacion.EN_CURSO)) > 0) {
            throw new NegocioException("La unidad " + u.getCodigo() + " tiene otro trabajo en curso; finalícelo primero");
        }
    }

    public void exigirEmpleadoLibre(Empleado e) {
        if (e != null && asignacionRepository.findByEmpleadoIdAndEstadoInOrderByFechaInicioAsc(e.getId(), List.of(EstadoAsignacion.EN_CURSO))
                .stream().findAny().isPresent()) {
            throw new NegocioException(e.getNombreCompleto() + " tiene otro trabajo en curso");
        }
    }

    public void notificarAsignacion(Asignacion a) {
        notificacionService.notificarEmpleado(a.getEmpleado(), "Nueva asignación " + a.getReferencia(),
                "Se le asignó la unidad " + a.getUnidad().getDescripcion() + " del " + FMT.format(a.getFechaInicio())
                        + " al " + FMT.format(a.getFechaFin()) + ".",
                TipoNotificacion.INFO, "/mis-tareas", null);
    }

    // ------------------------------------------------------------ Disponibilidad

    @Transactional(readOnly = true)
    public List<Unidad> unidadesDisponibles(LocalDateTime inicio, LocalDateTime fin, TipoUnidad tipo) {
        return unidadService.listarOperativas().stream()
                .filter(u -> tipo == null || u.getTipo() == tipo)
                .filter(u -> asignacionRepository.cruceUnidad(u.getId(), inicio, fin, EstadosActivos.ASIGNACION, 0L).isEmpty())
                .filter(u -> ordenRepository.programadasEnRango(u.getId(), inicio.toLocalDate(), fin.toLocalDate(),
                        EstadosActivos.ORDEN).isEmpty())
                .toList();
    }

    @Transactional(readOnly = true)
    public List<Empleado> personalDisponible(LocalDateTime inicio, LocalDateTime fin) {
        return empleadoService.listarPersonalDeCampo().stream()
                .filter(e -> e.getLicenciaVencimiento() == null || !e.getLicenciaVencimiento().isBefore(fin.toLocalDate()))
                .filter(e -> asignacionRepository.cruceEmpleado(e.getId(), inicio, fin, EstadosActivos.ASIGNACION, 0L).isEmpty())
                .toList();
    }

    private static String nvl(String a, String b) {
        return a != null ? a : b;
    }
}
