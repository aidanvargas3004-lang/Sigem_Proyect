package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.model.Operacion;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoAsignacion;
import com.transmaqsur.sigem.model.enums.EstadoContrato;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.model.enums.OrigenAsignacion;
import com.transmaqsur.sigem.model.enums.TipoNotificacion;
import com.transmaqsur.sigem.util.Entidades;
import com.transmaqsur.sigem.util.Montos;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Ciclo de vida común de alquileres y servicios de transporte:
 * programado → en curso → finalizado (calcula lo facturable) o anulado.
 */
@Transactional
public abstract class OperacionService<T extends Operacion> {

    private static final String[] CAMPOS_PROTEGIDOS = {"codigo", "asignacion", "estado", "lecturaInicial", "lecturaFinal",
            "inicioReal", "finReal", "cantidadFacturable", "montoTotal", "facturado"};

    protected final JpaRepository<T, Long> repository;
    protected final AsignacionService asignacionService;
    protected final UnidadService unidadService;
    protected final NotificacionService notificacionService;
    protected final AlertaService alertaService;

    protected OperacionService(JpaRepository<T, Long> repository, AsignacionService asignacionService, UnidadService unidadService,
                               NotificacionService notificacionService, AlertaService alertaService) {
        this.repository = repository;
        this.asignacionService = asignacionService;
        this.unidadService = unidadService;
        this.notificacionService = notificacionService;
        this.alertaService = alertaService;
    }

    /** Prefijo del código (ALQ, SRV). */
    protected abstract String prefijo();

    protected abstract OrigenAsignacion origen();

    protected abstract String nombre();

    protected abstract String ruta();

    /** Reglas propias del tipo de operación (modalidades permitidas, tipo de unidad...). */
    protected abstract void validarEspecifico(T operacion, Unidad unidad);

    @Transactional(readOnly = true)
    public T obtener(Long id) {
        return repository.findById(id).orElseThrow(() -> new NoEncontradoException(nombre(), id));
    }

    public T guardar(T form) {
        Asignacion af = form.getAsignacion();
        if (af == null || af.getUnidad() == null) {
            throw new NegocioException("Seleccione la unidad");
        }
        Unidad unidad = unidadService.obtener(af.getUnidad().getId());
        validarContrato(form);
        validarEspecifico(form, unidad);
        completarTarifa(form, unidad);

        if (form.isNuevo()) {
            af.setOrigen(origen());
            af.setEstado(EstadoAsignacion.PROGRAMADA);
            asignacionService.validar(af, 0L);
            form.setEstado(EstadoOperacion.PROGRAMADO);
            form.setFacturado(false);
            T op = repository.save(form);
            op.setCodigo(Entidades.codigo(prefijo(), op.getId()));
            op.getAsignacion().setReferencia(op.getCodigo());
            op.getAsignacion().setDescripcion(nombre() + " para " + op.getCliente().getRazonSocial());
            unidadService.recalcularEstado(unidad);
            asignacionService.notificarAsignacion(op.getAsignacion());
            return op;
        }

        T op = obtener(form.getId());
        if (op.getEstado() != EstadoOperacion.PROGRAMADO) {
            throw new NegocioException("Solo se pueden editar operaciones programadas");
        }
        Unidad anterior = op.getUnidad();
        Entidades.copiar(form, op, CAMPOS_PROTEGIDOS);
        Asignacion a = op.getAsignacion();
        a.setUnidad(unidad);
        a.setEmpleado(af.getEmpleado());
        a.setFechaInicio(af.getFechaInicio());
        a.setFechaFin(af.getFechaFin());
        asignacionService.validar(a, a.getId());
        unidadService.recalcularEstado(anterior);
        unidadService.recalcularEstado(unidad);
        return op;
    }

    /** Inicio del trabajo: registra la lectura inicial y pone la unidad en servicio. */
    public void iniciar(Long id, BigDecimal lecturaInicial, LocalDateTime fechaHora) {
        T op = obtener(id);
        if (op.getEstado() != EstadoOperacion.PROGRAMADO) {
            throw new NegocioException("Solo se pueden iniciar operaciones programadas");
        }
        Unidad u = op.getUnidad();
        asignacionService.exigirUnidadLibre(u);
        asignacionService.exigirEmpleadoLibre(op.getEmpleado());
        BigDecimal lectura = lecturaInicial != null ? lecturaInicial : u.getLecturaActual();
        unidadService.actualizarLectura(u, lectura);
        op.setLecturaInicial(lectura);
        op.setInicioReal(fechaHora != null ? fechaHora : LocalDateTime.now());
        op.setEstado(EstadoOperacion.EN_CURSO);
        op.getAsignacion().setEstado(EstadoAsignacion.EN_CURSO);
        unidadService.recalcularEstado(u);
    }

    /** Cierre del trabajo: registra la lectura final y calcula horas/días/km facturables y el monto. */
    public void finalizar(Long id, BigDecimal lecturaFinal, LocalDateTime fechaHora) {
        T op = obtener(id);
        if (op.getEstado() != EstadoOperacion.EN_CURSO) {
            throw new NegocioException("Solo se pueden finalizar operaciones en curso");
        }
        Unidad u = op.getUnidad();
        if (lecturaFinal == null) {
            throw new NegocioException("Registre la lectura final de la unidad (" + u.getTipo().getNombreMedida() + ")");
        }
        if (lecturaFinal.compareTo(op.getLecturaInicial()) < 0) {
            throw new NegocioException("La lectura final no puede ser menor que la inicial (" + op.getLecturaInicial().toPlainString() + ")");
        }
        LocalDateTime fin = fechaHora != null ? fechaHora : LocalDateTime.now();
        if (!fin.isAfter(op.getInicioReal())) {
            throw new NegocioException("La fecha de fin debe ser posterior al inicio real (" + AsignacionService.FMT.format(op.getInicioReal()) + ")");
        }
        unidadService.actualizarLectura(u, lecturaFinal);
        op.setLecturaFinal(lecturaFinal);
        op.setFinReal(fin);
        BigDecimal cantidad = calcularCantidad(op, u);
        op.setCantidadFacturable(cantidad);
        op.setMontoTotal(Montos.multiplicar(cantidad, op.getTarifa()));
        op.setEstado(EstadoOperacion.FINALIZADO);
        op.getAsignacion().setEstado(EstadoAsignacion.FINALIZADA);
        unidadService.recalcularEstado(u);
        alertaService.revisarMantenimiento(u);
        notificacionService.notificarPermiso(Modulo.VENTAS.getPermisoGestionar(), op.getCodigo() + " lista para facturar",
                op.getCliente().getRazonSocial() + ": " + cantidad.stripTrailingZeros().toPlainString() + " "
                        + op.getModalidad().getUnidad() + " × S/ " + op.getTarifa() + " = S/ " + op.getMontoTotal(),
                TipoNotificacion.EXITO, ruta() + "/" + op.getId(), null);
    }

    public void anular(Long id, String motivo) {
        T op = obtener(id);
        if (op.getEstado() != EstadoOperacion.PROGRAMADO) {
            throw new NegocioException("Solo se anulan operaciones programadas; las que están en curso deben finalizarse");
        }
        op.setEstado(EstadoOperacion.ANULADO);
        op.getAsignacion().setEstado(EstadoAsignacion.CANCELADA);
        if (motivo != null && !motivo.isBlank()) {
            op.setObservaciones((op.getObservaciones() != null ? op.getObservaciones() + "\n" : "") + "Anulado: " + motivo);
        }
        unidadService.recalcularEstado(op.getUnidad());
    }

    /** Cantidad facturable según la modalidad de cobro. */
    static BigDecimal calcularCantidad(Operacion op, Unidad u) {
        BigDecimal diferencia = op.getLecturaFinal().subtract(op.getLecturaInicial());
        return switch (op.getModalidad()) {
            case POR_HORA -> u.getTipo().isVehiculo()
                    ? BigDecimal.valueOf(Duration.between(op.getInicioReal(), op.getFinReal()).toMinutes())
                        .divide(BigDecimal.valueOf(60), 2, RoundingMode.HALF_UP)
                    : diferencia;
            case POR_DIA -> BigDecimal.valueOf(ChronoUnit.DAYS.between(op.getInicioReal().toLocalDate(), op.getFinReal().toLocalDate()) + 1);
            case POR_KM -> diferencia;
            case POR_VIAJE -> BigDecimal.ONE;
        };
    }

    private void validarContrato(T op) {
        Contrato c = op.getContrato();
        if (c == null) {
            return;
        }
        if (c.getEstado() != EstadoContrato.VIGENTE) {
            throw new NegocioException("El contrato " + c.getCodigo() + " no está vigente");
        }
        if (!c.getCliente().getId().equals(op.getCliente().getId())) {
            throw new NegocioException("El contrato " + c.getCodigo() + " pertenece a otro cliente");
        }
        Asignacion a = op.getAsignacion();
        if (a.getFechaInicio() != null && a.getFechaFin() != null
                && (a.getFechaInicio().toLocalDate().isBefore(c.getFechaInicio()) || a.getFechaFin().toLocalDate().isAfter(c.getFechaFin()))) {
            throw new NegocioException("Las fechas están fuera de la vigencia del contrato " + c.getCodigo()
                    + " (" + c.getFechaInicio() + " al " + c.getFechaFin() + ")");
        }
    }

    private void completarTarifa(T op, Unidad u) {
        if (op.getTarifa() != null) {
            return;
        }
        BigDecimal tarifa = switch (op.getModalidad()) {
            case POR_HORA -> u.getTarifaHora();
            case POR_DIA -> u.getTarifaDia();
            case POR_KM -> u.getTarifaKm();
            case POR_VIAJE -> null;
        };
        if (tarifa == null) {
            throw new NegocioException("Ingrese la tarifa: la unidad " + u.getCodigo() + " no tiene tarifa "
                    + op.getModalidad().getLabel().toLowerCase() + " registrada");
        }
        op.setTarifa(tarifa);
    }
}
