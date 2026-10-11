package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.model.Asignacion;
import com.transmaqsur.sigem.model.Contrato;
import com.transmaqsur.sigem.model.Cotizacion;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.Inventario;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoAsignacion;
import com.transmaqsur.sigem.model.enums.EstadoContrato;
import com.transmaqsur.sigem.model.enums.EstadoEmpleado;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.EstadoUnidad;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.model.enums.TipoNotificacion;
import com.transmaqsur.sigem.repository.AlquilerRepository;
import com.transmaqsur.sigem.repository.AsignacionRepository;
import com.transmaqsur.sigem.repository.ContratoRepository;
import com.transmaqsur.sigem.repository.CotizacionRepository;
import com.transmaqsur.sigem.repository.EmpleadoRepository;
import com.transmaqsur.sigem.repository.InventarioRepository;
import com.transmaqsur.sigem.repository.ServicioTransporteRepository;
import com.transmaqsur.sigem.repository.UnidadRepository;
import com.transmaqsur.sigem.model.enums.EstadoCotizacion;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.IsoFields;
import java.util.List;

/**
 * Genera las notificaciones automáticas del sistema: mantenimientos preventivos
 * próximos, stock bajo, contratos y cotizaciones por vencer, licencias, SOAT y
 * revisiones técnicas. Se ejecuta al iniciar la aplicación y cada hora.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AlertaService {

    static final int DIAS_ANTICIPACION = 30;

    private final NotificacionService notificacionService;
    private final UnidadRepository unidadRepository;
    private final EmpleadoRepository empleadoRepository;
    private final InventarioRepository inventarioRepository;
    private final ContratoRepository contratoRepository;
    private final CotizacionRepository cotizacionRepository;
    private final AlquilerRepository alquilerRepository;
    private final ServicioTransporteRepository servicioRepository;
    private final AsignacionRepository asignacionRepository;

    public void generarAlertas() {
        LocalDate hoy = LocalDate.now();
        LocalDate limite = hoy.plusDays(DIAS_ANTICIPACION);

        // Mantenimiento preventivo y documentos de las unidades
        for (Unidad u : unidadRepository.findByEstadoNotOrderByCodigoAsc(EstadoUnidad.FUERA_SERVICIO)) {
            revisarMantenimiento(u);
            revisarDocumento(u, "SOAT", u.getVencimientoSoat(), limite);
            revisarDocumento(u, "Revisión técnica", u.getVencimientoRevisionTecnica(), limite);
        }

        // Stock bajo mínimo
        inventarioRepository.findBajoMinimo().forEach(this::revisarStock);

        // Licencias de conducir
        for (Empleado e : empleadoRepository.findByEstadoAndLicenciaVencimientoBefore(EstadoEmpleado.ACTIVO, limite)) {
            boolean vencida = e.getLicenciaVencimiento().isBefore(hoy);
            String msg = "La licencia " + e.getLicenciaNumero() + " de " + e.getNombreCompleto()
                    + (vencida ? " venció el " : " vence el ") + e.getLicenciaVencimiento() + ".";
            String clave = "LIC-" + e.getId() + "-" + e.getLicenciaVencimiento() + (vencida ? "-V" : "");
            TipoNotificacion tipo = vencida ? TipoNotificacion.PELIGRO : TipoNotificacion.ALERTA;
            notificacionService.notificarPermiso(Modulo.EMPLEADOS.getPermisoGestionar(), "Licencia por vencer", msg, tipo,
                    "/empleados/" + e.getId(), clave);
            notificacionService.notificarEmpleado(e, "Su licencia está por vencer", msg, tipo, "/perfil", clave);
        }

        // Cotizaciones cuya validez terminó
        List<Cotizacion> vencidas = cotizacionRepository.findByEstado(EstadoCotizacion.ENVIADA).stream()
                .filter(c -> c.getFechaVencimiento().isBefore(hoy)).toList();
        for (Cotizacion c : vencidas) {
            c.setEstado(EstadoCotizacion.VENCIDA);
            notificacionService.notificarPermiso(Modulo.SOLICITUDES.getPermisoGestionar(), "Cotización vencida",
                    "La cotización " + c.getCodigo() + " de " + c.getCliente().getRazonSocial() + " venció sin respuesta.",
                    TipoNotificacion.INFO, "/cotizaciones/" + c.getId(), "COT-VENC-" + c.getId());
        }

        // Contratos por vencer y vencidos
        for (Contrato c : contratoRepository.findByEstadoOrderByFechaFinAsc(EstadoContrato.VIGENTE)) {
            if (c.getFechaFin().isBefore(hoy)) {
                finalizarVencido(c);
            } else if (!c.getFechaFin().isAfter(limite)) {
                notificacionService.notificarPermiso(Modulo.CONTRATOS.getPermisoGestionar(), "Contrato por vencer",
                        "El contrato " + c.getCodigo() + " con " + c.getCliente().getRazonSocial() + " vence el " + c.getFechaFin()
                                + " (" + c.getDiasRestantes() + " días). Coordine la renovación.",
                        TipoNotificacion.ALERTA, "/contratos/" + c.getId(), "CTR-" + c.getId() + "-" + c.getFechaFin());
            }
        }
        // Servicios y alquileres en curso cuya hora de fin programada ya pasó (pendientes de cierre)
        LocalDateTime ahora = LocalDateTime.now();
        for (Asignacion a : asignacionRepository.findByEstadoInOrderByFechaInicioAsc(List.of(EstadoAsignacion.EN_CURSO))) {
            if (a.getFechaFin().isBefore(ahora)) {
                notificacionService.notificarPermiso(Modulo.ASIGNACIONES.getPermisoGestionar(), "Servicio pendiente de cierre",
                        a.getReferencia() + " (" + a.getUnidad().getCodigo() + ") debía terminar el " + a.getFechaFin().toLocalDate()
                                + " y sigue en curso. Registre la lectura final para cerrarlo y facturarlo.",
                        TipoNotificacion.ALERTA, "/asignaciones/" + a.getId(), "CIERRE-" + a.getId() + "-" + a.getFechaFin().toLocalDate());
                notificacionService.notificarEmpleado(a.getEmpleado(), "Cierre pendiente de " + a.getReferencia(),
                        "Registre la lectura final y la hora de término del trabajo con " + a.getUnidad().getCodigo() + ".",
                        TipoNotificacion.ALERTA, "/mis-tareas", "CIERRE-" + a.getId() + "-" + a.getFechaFin().toLocalDate());
            }
        }
        log.info("Alertas automáticas revisadas");
    }

    public void revisarMantenimiento(Unidad u) {
        if (!u.isRequiereMantenimiento() || u.getEstado() == EstadoUnidad.FUERA_SERVICIO) {
            return;
        }
        boolean vencido = u.isMantenimientoVencido();
        String medida = u.getTipo().getMedida();
        StringBuilder msg = new StringBuilder(u.getDescripcion()).append(": ");
        msg.append("lectura ").append(u.getLecturaActual().stripTrailingZeros().toPlainString()).append(' ').append(medida)
                .append(", preventivo a los ").append(u.getProximoMantenimiento().stripTrailingZeros().toPlainString()).append(' ').append(medida);
        if (u.getProximaFechaMantenimiento() != null) {
            msg.append(" o el ").append(u.getProximaFechaMantenimiento());
        }
        msg.append(vencido ? " (vencido)." : " (próximo).");
        notificacionService.notificarPermiso(Modulo.MANTENIMIENTO.getPermisoGestionar(),
                vencido ? "Mantenimiento preventivo vencido" : "Mantenimiento preventivo próximo", msg.toString(),
                vencido ? TipoNotificacion.PELIGRO : TipoNotificacion.ALERTA,
                "/mantenimiento/nuevo?unidadId=" + u.getId(),
                "MANT-" + u.getId() + "-" + u.getProximoMantenimiento().toPlainString() + "-" + u.getProximaFechaMantenimiento()
                        + (vencido ? "-V" : ""));
    }

    public void revisarStock(Inventario inv) {
        if (!inv.isBajoMinimo()) {
            return;
        }
        LocalDate hoy = LocalDate.now();
        String clave = "STOCK-" + inv.getId() + "-" + hoy.getYear() + "W" + hoy.get(IsoFields.WEEK_OF_WEEK_BASED_YEAR);
        String msg = inv.getRepuesto().getDescripcionCompleta() + " en " + inv.getAlmacen().getNombre() + ": stock "
                + inv.getCantidad().stripTrailingZeros().toPlainString() + " (mínimo "
                + inv.getRepuesto().getStockMinimo().stripTrailingZeros().toPlainString() + "). Genere una orden de compra.";
        notificacionService.notificarPermiso(Modulo.COMPRAS.getPermisoGestionar(), "Stock bajo mínimo", msg,
                TipoNotificacion.ALERTA, "/inventario", clave);
        notificacionService.notificarPermiso(Modulo.ALMACENES.getPermisoGestionar(), "Stock bajo mínimo", msg,
                TipoNotificacion.ALERTA, "/inventario", clave);
    }

    private void revisarDocumento(Unidad u, String documento, LocalDate vencimiento, LocalDate limite) {
        if (vencimiento == null || vencimiento.isAfter(limite)) {
            return;
        }
        boolean vencido = vencimiento.isBefore(LocalDate.now());
        notificacionService.notificarPermiso(Modulo.FLOTA.getPermisoGestionar(), documento + (vencido ? " vencido" : " por vencer"),
                "El " + documento + " de " + u.getDescripcion() + (vencido ? " venció el " : " vence el ") + vencimiento + ".",
                vencido ? TipoNotificacion.PELIGRO : TipoNotificacion.ALERTA, "/unidades/" + u.getId(),
                documento.substring(0, 4).toUpperCase() + "-" + u.getId() + "-" + vencimiento + (vencido ? "-V" : ""));
    }

    private void finalizarVencido(Contrato c) {
        boolean activas = alquilerRepository.findByContratoIdOrderByIdDesc(c.getId()).stream().anyMatch(a -> activa(a.getEstado()))
                || servicioRepository.findByContratoIdOrderByIdDesc(c.getId()).stream().anyMatch(s -> activa(s.getEstado()));
        if (activas) {
            notificacionService.notificarPermiso(Modulo.CONTRATOS.getPermisoGestionar(), "Contrato vencido con operaciones activas",
                    "El contrato " + c.getCodigo() + " venció el " + c.getFechaFin() + " pero tiene operaciones abiertas. Renuévelo o ciérrelas.",
                    TipoNotificacion.PELIGRO, "/contratos/" + c.getId(), "CTR-VENC-" + c.getId() + "-" + c.getFechaFin());
            return;
        }
        c.setEstado(EstadoContrato.FINALIZADO);
        notificacionService.notificarPermiso(Modulo.CONTRATOS.getPermisoGestionar(), "Contrato finalizado",
                "El contrato " + c.getCodigo() + " con " + c.getCliente().getRazonSocial() + " se finalizó automáticamente al vencer.",
                TipoNotificacion.INFO, "/contratos/" + c.getId(), "CTR-FIN-" + c.getId());
    }

    private static boolean activa(EstadoOperacion e) {
        return e == EstadoOperacion.PROGRAMADO || e == EstadoOperacion.EN_CURSO;
    }
}
