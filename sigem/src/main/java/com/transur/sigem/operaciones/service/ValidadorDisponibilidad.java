package com.transur.sigem.operaciones.service;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import com.transur.sigem.mantenimiento.repository.MantenimientoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.operaciones.entity.Asignacion;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;

/**
 * Regla de negocio RF009 "Validación de conflictos": impide asignar una unidad o
 * un conductor/operador a dos servicios que se cruzan en el tiempo, una unidad con
 * mantenimiento programado en ese rango, y personal con la licencia vencida.
 * <p>
 * Está separada de AsignacionService (principio de responsabilidad única), así
 * puede probarse y reutilizarse sola.
 */
@Component
@RequiredArgsConstructor
public class ValidadorDisponibilidad {

    static final List<String> ESTADOS_NO_ASIGNABLES = List.of("VENDIDO", "BAJA_DEFINITIVA", "FUERA_DE_SERVICIO");
    static final List<String> MANTENIMIENTO_ABIERTO = List.of("PROGRAMADO", "EN_PROCESO");
    private static final DateTimeFormatter F = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final AsignacionRepository asignacionRepository;
    private final MantenimientoRepository mantenimientoRepository;
    private final DocumentoEmpleadoRepository documentoRepository;

    public void validarUnidad(Maquinaria m, Servicio servicio) {
        if (ESTADOS_NO_ASIGNABLES.contains(m.getEstadoOperativo())) {
            throw new NegocioException("La unidad " + m.getCodigoInterno() + " no puede asignarse (" + m.getEstadoOperativo() + ")");
        }
        if (m.getEstadoOperativo() != null && m.getEstadoOperativo().startsWith("EN_MANTENIMIENTO")) {
            throw new NegocioException("La unidad " + m.getCodigoInterno() + " está en mantenimiento");
        }
        LocalDateTime ini = servicio.getFechaProgramada();
        LocalDateTime fin = servicio.getFinOcupacion();
        Optional<Asignacion> cruce = asignacionRepository.findByMaquinariaIdAndEstado(m.getId(), "ACTIVA").stream()
                .filter(a -> !a.getServicio().getId().equals(servicio.getId()))
                .filter(a -> seCruzan(ini, fin, a.getServicio()))
                .findFirst();
        if (cruce.isPresent()) {
            Servicio otro = cruce.get().getServicio();
            throw new NegocioException("La unidad " + m.getCodigoInterno() + " ya está asignada al servicio SER-" + otro.getId()
                    + " del " + F.format(otro.getFechaProgramada()) + " al " + F.format(otro.getFinOcupacion())
                    + ". No se permite la doble asignación.");
        }
        List<Mantenimiento> mants = mantenimientoRepository.findByMaquinariaIdAndEstadoInAndFechaProgramadaBetween(
                m.getId(), MANTENIMIENTO_ABIERTO, ini.toLocalDate(), fin.toLocalDate());
        if (!mants.isEmpty()) {
            throw new NegocioException("La unidad " + m.getCodigoInterno() + " tiene un mantenimiento programado el "
                    + mants.get(0).getFechaProgramada() + ". Reprograme el mantenimiento o elija otra unidad.");
        }
    }

    public void validarOperador(Empleado e, Servicio servicio) {
        if (!Boolean.TRUE.equals(e.getEstado())) {
            throw new NegocioException("El empleado " + nombre(e) + " está inactivo");
        }
        String cargo = e.getCargo() == null ? "" : e.getCargo().trim().toUpperCase();
        if (!List.of("CONDUCTOR", "OPERADOR").contains(cargo)) {
            throw new NegocioException("El empleado seleccionado no está habilitado como operador/conductor");
        }
        Optional<Asignacion> cruce = asignacionRepository.findByEmpleadoIdAndEstado(e.getId(), "ACTIVA").stream()
                .filter(a -> !a.getServicio().getId().equals(servicio.getId()))
                .filter(a -> seCruzan(servicio.getFechaProgramada(), servicio.getFinOcupacion(), a.getServicio()))
                .findFirst();
        if (cruce.isPresent()) {
            throw new NegocioException(nombre(e) + " ya está asignado al servicio SER-" + cruce.get().getServicio().getId()
                    + " en ese horario");
        }
        documentoRepository.findByEmpleadoId(e.getId()).stream()
                .filter(d -> Boolean.TRUE.equals(d.getEstado()))
                .filter(d -> d.getTipoDocumento() != null && d.getTipoDocumento().toUpperCase().startsWith("LICENCIA"))
                .filter(d -> d.getFechaVencimiento() != null
                        && d.getFechaVencimiento().isBefore(servicio.getFinOcupacion().toLocalDate()))
                .findFirst()
                .ifPresent(d -> {
                    throw new NegocioException("La licencia " + d.getNumeroDocumento() + " de " + nombre(e)
                            + " vence el " + d.getFechaVencimiento() + ", antes de terminar el servicio");
                });
    }

    /** Dos rangos [ini, fin) se cruzan si cada uno empieza antes de que termine el otro. */
    static boolean seCruzan(LocalDateTime ini, LocalDateTime fin, Servicio otro) {
        return ini.isBefore(otro.getFinOcupacion()) && otro.getFechaProgramada().isBefore(fin);
    }

    private static String nombre(Empleado e) {
        return e.getNombres() + " " + e.getApellidos();
    }
}
