package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.dto.IncidenciaForm;
import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Incidencia;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.enums.EstadoIncidencia;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoMantenimiento;
import com.transmaqsur.sigem.model.enums.TipoNotificacion;
import com.transmaqsur.sigem.repository.IncidenciaRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Incidencias de servicio (RF011 / CU006): las reporta el conductor u operador,
 * Operaciones las atiende y Mantenimiento puede generar una orden correctiva.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class IncidenciaService {

    private final IncidenciaRepository incidenciaRepository;
    private final AsignacionService asignacionService;
    private final UnidadService unidadService;
    private final EmpleadoService empleadoService;
    private final MantenimientoService mantenimientoService;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<Incidencia> listar() {
        return incidenciaRepository.findAllByOrderByFechaHoraDesc();
    }

    @Transactional(readOnly = true)
    public List<Incidencia> deUnidad(Long unidadId) {
        return incidenciaRepository.findByUnidadIdOrderByFechaHoraDesc(unidadId);
    }

    @Transactional(readOnly = true)
    public List<Incidencia> deAsignacion(Long asignacionId) {
        return incidenciaRepository.findByAsignacionIdOrderByFechaHoraDesc(asignacionId);
    }

    @Transactional(readOnly = true)
    public long abiertas() {
        return incidenciaRepository.countByEstadoIn(List.of(EstadoIncidencia.REPORTADA, EstadoIncidencia.EN_ATENCION));
    }

    @Transactional(readOnly = true)
    public Incidencia obtener(Long id) {
        return incidenciaRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Incidencia", id));
    }

    /** Convierte el DTO del formulario en la entidad y la registra. */
    public Incidencia registrar(IncidenciaForm form, Long empleadoId) {
        Incidencia i = new Incidencia();
        if (form.getAsignacionId() != null) {
            Asignacion a = asignacionService.obtener(form.getAsignacionId());
            i.setAsignacion(a);
            i.setUnidad(a.getUnidad());
        } else if (form.getUnidadId() != null) {
            i.setUnidad(unidadService.obtener(form.getUnidadId()));
        } else {
            throw new NegocioException("Seleccione la unidad o el servicio en el que ocurrió la incidencia");
        }
        if (form.getFechaHora().isAfter(LocalDateTime.now().plusMinutes(5))) {
            throw new NegocioException("La fecha y hora de la incidencia no puede ser futura");
        }
        i.setTipo(form.getTipo());
        i.setSeveridad(form.getSeveridad());
        i.setFechaHora(form.getFechaHora());
        i.setDescripcion(form.getDescripcion().trim());
        i.setUbicacion(form.getUbicacion());
        i.setEstado(EstadoIncidencia.REPORTADA);
        if (empleadoId != null) {
            i.setReportadoPor(empleadoService.obtener(empleadoId));
        }
        Incidencia guardada = incidenciaRepository.save(i);
        guardada.setCodigo(Entidades.codigo("INC", guardada.getId()));
        notificar(guardada);
        return guardada;
    }

    public void atender(Long id) {
        Incidencia i = obtener(id);
        if (i.getEstado() != EstadoIncidencia.REPORTADA) {
            throw new NegocioException("La incidencia ya está " + i.getEstado().getLabel().toLowerCase());
        }
        i.setEstado(EstadoIncidencia.EN_ATENCION);
    }

    /** Genera una orden de mantenimiento correctivo para la unidad afectada. */
    public OrdenMantenimiento generarOrden(Long id) {
        Incidencia i = obtener(id);
        if (!i.isAbierta()) {
            throw new NegocioException("La incidencia ya fue resuelta");
        }
        if (i.getOrdenMantenimiento() != null) {
            throw new NegocioException("La incidencia ya tiene la orden " + i.getOrdenMantenimiento().getCodigo());
        }
        OrdenMantenimiento o = new OrdenMantenimiento();
        o.setUnidad(i.getUnidad());
        o.setTipo(TipoMantenimiento.CORRECTIVO);
        // Una incidencia alta o crítica se atiende aunque la unidad tenga trabajos programados
        o.setPrioridad(i.getSeveridad() == Prioridad.ALTA ? Prioridad.CRITICA : i.getSeveridad());
        o.setFechaProgramada(LocalDate.now());
        o.setDescripcion("Atención de la incidencia " + i.getCodigo() + " (" + i.getTipo().getLabel() + "): " + i.getDescripcion());
        o = mantenimientoService.guardar(o);
        i.setOrdenMantenimiento(o);
        i.setEstado(EstadoIncidencia.EN_ATENCION);
        return o;
    }

    public void resolver(Long id, String solucion) {
        Incidencia i = obtener(id);
        if (!i.isAbierta()) {
            throw new NegocioException("La incidencia ya fue resuelta");
        }
        if (solucion == null || solucion.isBlank()) {
            throw new NegocioException("Describa cómo se resolvió la incidencia");
        }
        i.setSolucion(solucion.trim());
        i.setFechaCierre(LocalDateTime.now());
        i.setEstado(EstadoIncidencia.RESUELTA);
    }

    private void notificar(Incidencia i) {
        TipoNotificacion tipo = i.getSeveridad() == Prioridad.CRITICA || i.getSeveridad() == Prioridad.ALTA
                ? TipoNotificacion.PELIGRO : TipoNotificacion.ALERTA;
        String msg = i.getTipo().getLabel() + " en " + i.getUnidad().getDescripcion()
                + (i.getAsignacion() != null ? " durante " + i.getAsignacion().getReferencia() : "") + ": " + i.getDescripcion();
        String enlace = "/incidencias/" + i.getId();
        notificacionService.notificarPermiso(Modulo.ASIGNACIONES.getPermisoGestionar(), "Incidencia " + i.getCodigo(), msg, tipo, enlace, null);
        if (i.getTipo().isAfectaUnidad()) {
            notificacionService.notificarPermiso(Modulo.MANTENIMIENTO.getPermisoGestionar(), "Incidencia " + i.getCodigo(), msg, tipo,
                    enlace, "INC-" + i.getId());
        }
    }
}
