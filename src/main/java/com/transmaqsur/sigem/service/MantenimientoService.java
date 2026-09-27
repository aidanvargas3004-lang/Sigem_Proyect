package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Almacen;
import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.OrdenMantenimiento;
import com.transmaqsur.sigem.model.Repuesto;
import com.transmaqsur.sigem.model.RepuestoUtilizado;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoOrden;
import com.transmaqsur.sigem.model.enums.Prioridad;
import com.transmaqsur.sigem.model.enums.TipoMantenimiento;
import com.transmaqsur.sigem.model.enums.TipoNotificacion;
import com.transmaqsur.sigem.repository.AsignacionRepository;
import com.transmaqsur.sigem.repository.OrdenMantenimientoRepository;
import com.transmaqsur.sigem.repository.RepuestoUtilizadoRepository;
import com.transmaqsur.sigem.util.Entidades;
import com.transmaqsur.sigem.util.Montos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Órdenes de mantenimiento preventivo y correctivo:
 * programada → en proceso (unidad en mantenimiento, consume repuestos) → completada.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class MantenimientoService {

    private final OrdenMantenimientoRepository ordenRepository;
    private final RepuestoUtilizadoRepository repuestoUtilizadoRepository;
    private final AsignacionRepository asignacionRepository;
    private final UnidadService unidadService;
    private final InventarioService inventarioService;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<OrdenMantenimiento> listar() {
        return ordenRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<OrdenMantenimiento> abiertas() {
        return ordenRepository.findByEstadoInOrderByFechaProgramadaAsc(EstadosActivos.ORDEN);
    }

    @Transactional(readOnly = true)
    public List<OrdenMantenimiento> deUnidad(Long unidadId) {
        return ordenRepository.findByUnidadIdOrderByIdDesc(unidadId);
    }

    @Transactional(readOnly = true)
    public List<OrdenMantenimiento> abiertasDeTecnico(Long empleadoId) {
        return ordenRepository.findByTecnicoIdAndEstadoInOrderByFechaProgramadaAsc(empleadoId, EstadosActivos.ORDEN);
    }

    @Transactional(readOnly = true)
    public List<RepuestoUtilizado> repuestosUtilizados() {
        return repuestoUtilizadoRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public OrdenMantenimiento obtener(Long id) {
        return ordenRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Orden de mantenimiento", id));
    }

    /** Formulario sugerido para un preventivo (desde las alertas). */
    @Transactional(readOnly = true)
    public OrdenMantenimiento nuevaPreventiva(Long unidadId) {
        Unidad u = unidadService.obtener(unidadId);
        OrdenMantenimiento o = new OrdenMantenimiento();
        o.setUnidad(u);
        o.setTipo(TipoMantenimiento.PREVENTIVO);
        o.setPrioridad(u.isMantenimientoVencido() ? Prioridad.ALTA : Prioridad.MEDIA);
        o.setFechaProgramada(LocalDate.now().plusDays(1));
        o.setDescripcion("Mantenimiento preventivo de " + u.getProximoMantenimiento().stripTrailingZeros().toPlainString() + " "
                + u.getTipo().getMedida() + ": cambio de aceite y filtros, revisión de frenos, sistema hidráulico, neumáticos/orugas y engrase general.");
        return o;
    }

    public OrdenMantenimiento guardar(OrdenMantenimiento form) {
        Unidad u = unidadService.obtener(form.getUnidad().getId());
        form.setUnidad(u);
        if (form.getPrioridad() != Prioridad.CRITICA) {
            // Evita programar el taller cuando la unidad ya está comprometida con un cliente
            List<Asignacion> cruces = asignacionRepository.cruceUnidad(u.getId(), form.getFechaProgramada().atStartOfDay(),
                    form.getFechaProgramada().plusDays(1).atStartOfDay(), EstadosActivos.ASIGNACION, 0L);
            if (!cruces.isEmpty()) {
                throw new NegocioException("La unidad " + u.getCodigo() + " está asignada a " + cruces.get(0).getReferencia()
                        + " ese día. Elija otra fecha o marque la prioridad como crítica.");
            }
        }
        if (form.isNuevo()) {
            form.setEstado(EstadoOrden.PROGRAMADA);
            if (form.getLecturaUnidad() == null) {
                form.setLecturaUnidad(u.getLecturaActual());
            }
            OrdenMantenimiento o = ordenRepository.save(form);
            o.setCodigo(Entidades.codigo("OT", o.getId()));
            notificacionService.notificarEmpleado(o.getTecnico(), "Orden " + o.getCodigo() + " asignada",
                    o.getTipo().getLabel() + " de " + u.getDescripcion() + " programado para el " + o.getFechaProgramada() + ".",
                    TipoNotificacion.INFO, "/mantenimiento/" + o.getId(), null);
            return o;
        }
        OrdenMantenimiento o = obtener(form.getId());
        if (o.getEstado() != EstadoOrden.PROGRAMADA) {
            throw new NegocioException("Solo se editan órdenes programadas");
        }
        Entidades.copiar(form, o, "codigo", "estado", "fechaInicio", "fechaFin", "costoRepuestos", "repuestos", "trabajoRealizado",
                "costoManoObra", "costoServiciosTerceros");
        return o;
    }

    /** Ingreso a taller: la unidad pasa a EN MANTENIMIENTO y queda bloqueada. */
    public void iniciar(Long id) {
        OrdenMantenimiento o = obtener(id);
        if (o.getEstado() != EstadoOrden.PROGRAMADA) {
            throw new NegocioException("Solo se inician órdenes programadas");
        }
        Unidad u = o.getUnidad();
        if (asignacionRepository.countByUnidadIdAndEstadoIn(u.getId(), List.of(com.transmaqsur.sigem.model.enums.EstadoAsignacion.EN_CURSO)) > 0) {
            throw new NegocioException("La unidad " + u.getCodigo() + " está trabajando; finalice primero el servicio en curso");
        }
        if (ordenRepository.existsByUnidadIdAndEstadoIn(u.getId(), List.of(EstadoOrden.EN_PROCESO))) {
            throw new NegocioException("La unidad ya tiene otra orden en proceso");
        }
        o.setFechaInicio(LocalDateTime.now());
        o.setLecturaUnidad(u.getLecturaActual());
        o.setEstado(EstadoOrden.EN_PROCESO);
        ordenRepository.flush();
        unidadService.recalcularEstado(u);
    }

    /** Consume un repuesto del almacén (descuenta stock y suma al costo de la orden). */
    public void agregarRepuesto(Long ordenId, Long repuestoId, Long almacenId, BigDecimal cantidad) {
        OrdenMantenimiento o = obtener(ordenId);
        if (o.getEstado() != EstadoOrden.EN_PROCESO) {
            throw new NegocioException("Los repuestos se registran con la orden en proceso");
        }
        Repuesto r = inventarioService.obtenerRepuesto(repuestoId);
        Almacen a = inventarioService.obtenerAlmacen(almacenId);
        inventarioService.salida(a, r, cantidad, o.getCodigo(), "Consumo en " + o.getUnidad().getCodigo());
        RepuestoUtilizado ru = new RepuestoUtilizado();
        ru.setOrden(o);
        ru.setRepuesto(r);
        ru.setAlmacen(a);
        ru.setCantidad(cantidad);
        ru.setPrecioUnitario(r.getPrecioUnitario());
        ru.setSubtotal(Montos.multiplicar(cantidad, r.getPrecioUnitario()));
        o.getRepuestos().add(ru);
        recalcularCosto(o);
    }

    /** Devuelve el repuesto al almacén (por error de registro o no utilizado). */
    public void quitarRepuesto(Long ordenId, Long repuestoUtilizadoId) {
        OrdenMantenimiento o = obtener(ordenId);
        if (o.getEstado() != EstadoOrden.EN_PROCESO) {
            throw new NegocioException("Solo se pueden devolver repuestos con la orden en proceso");
        }
        RepuestoUtilizado ru = o.getRepuestos().stream().filter(x -> x.getId().equals(repuestoUtilizadoId)).findFirst()
                .orElseThrow(() -> new NoEncontradoException("Repuesto utilizado", repuestoUtilizadoId));
        inventarioService.entrada(ru.getAlmacen(), ru.getRepuesto(), ru.getCantidad(), o.getCodigo(), "Devolución de " + o.getCodigo());
        o.getRepuestos().remove(ru);
        recalcularCosto(o);
    }

    /** Cierre de la orden: la unidad vuelve a estar disponible y, si es preventivo, reinicia el contador. */
    public void completar(Long id, String trabajoRealizado, BigDecimal costoManoObra, BigDecimal costoTerceros, BigDecimal lectura) {
        OrdenMantenimiento o = obtener(id);
        if (o.getEstado() != EstadoOrden.EN_PROCESO) {
            throw new NegocioException("Solo se completan órdenes en proceso");
        }
        if (trabajoRealizado == null || trabajoRealizado.isBlank()) {
            throw new NegocioException("Describa el trabajo realizado");
        }
        Unidad u = o.getUnidad();
        unidadService.actualizarLectura(u, lectura);
        o.setTrabajoRealizado(trabajoRealizado);
        o.setCostoManoObra(Montos.redondear(costoManoObra));
        o.setCostoServiciosTerceros(Montos.redondear(costoTerceros));
        o.setLecturaUnidad(u.getLecturaActual());
        o.setFechaFin(LocalDateTime.now());
        o.setEstado(EstadoOrden.COMPLETADA);
        if (o.getTipo() == TipoMantenimiento.PREVENTIVO) {
            u.setLecturaUltimoMantenimiento(u.getLecturaActual());
        }
        ordenRepository.flush();
        unidadService.recalcularEstado(u);
    }

    public void cancelar(Long id) {
        OrdenMantenimiento o = obtener(id);
        if (!o.isAbierta()) {
            throw new NegocioException("La orden ya está " + o.getEstado().getLabel().toLowerCase());
        }
        if (!o.getRepuestos().isEmpty()) {
            throw new NegocioException("La orden tiene repuestos registrados; devuélvalos al almacén antes de cancelarla");
        }
        o.setEstado(EstadoOrden.CANCELADA);
        ordenRepository.flush();
        unidadService.recalcularEstado(o.getUnidad());
    }

    private void recalcularCosto(OrdenMantenimiento o) {
        o.setCostoRepuestos(Montos.redondear(o.getRepuestos().stream().map(RepuestoUtilizado::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)));
    }
}
