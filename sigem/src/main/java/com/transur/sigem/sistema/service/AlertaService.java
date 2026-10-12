package com.transur.sigem.sistema.service;

import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.mantenimiento.entity.PlanMantenimiento;
import com.transur.sigem.mantenimiento.repository.PlanMantenimientoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.repository.AsignacionRepository;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.rrhh.entity.DocumentoEmpleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;

/**
 * RF014 - Genera alertas automáticas y las entrega como notificaciones a los roles responsables.
 * Cada regla usa una clave estable para no duplicar la misma alerta.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AlertaService {

    static final int DIAS_DOCUMENTOS = 30;
    static final int DIAS_DOCUMENTOS_UNIDAD = 30;
    static final int DIAS_CONTRATOS = 15;
    static final int HORAS_SERVICIO_SIN_ASIGNAR = 48;
    private static final DateTimeFormatter FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private final NotificacionService notificacionService;
    private final UsuarioRepository usuarioRepository;
    private final DocumentoEmpleadoRepository documentoRepository;
    private final RepuestoRepository repuestoRepository;
    private final PlanMantenimientoRepository planRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final ContratoRepository contratoRepository;
    private final ServicioRepository servicioRepository;
    private final AsignacionRepository asignacionRepository;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void alIniciar() {
        try {
            log.info("Alertas generadas al iniciar: {}", generarAlertas());
        } catch (RuntimeException e) {
            log.warn("No se pudieron generar alertas al iniciar: {}", e.getMessage());
        }
    }

    /** Revisión cada hora. */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void programada() {
        generarAlertas();
    }

    /** Ejecuta todas las reglas y devuelve cuántas notificaciones nuevas se crearon. */
    @Transactional
    public int generarAlertas() {
        LocalDate hoy = LocalDate.now();
        return documentosEmpleado(hoy) + stockMinimo() + planesPreventivos()
                + documentosUnidad(hoy) + contratosPorVencer(hoy) + serviciosSinAsignar();
    }

    private int documentosEmpleado(LocalDate hoy) {
        List<Usuario> destino = usuarios("RRHH", "ADMIN");
        int n = 0;
        for (DocumentoEmpleado d : documentoRepository.findAll()) {
            if (!Boolean.TRUE.equals(d.getEstado()) || d.getFechaVencimiento() == null
                    || d.getFechaVencimiento().isAfter(hoy.plusDays(DIAS_DOCUMENTOS))) continue;
            boolean vencido = d.getFechaVencimiento().isBefore(hoy);
            String quien = d.getEmpleado() != null ? d.getEmpleado().getNombres() + " " + d.getEmpleado().getApellidos() : "-";
            n += enviar(destino, vencido ? "Documento vencido" : "Documento por vencer",
                    d.getTipoDocumento() + " de " + quien + (vencido ? " venció el " : " vence el ") + d.getFechaVencimiento().format(FECHA),
                    vencido ? "ERROR" : "WARNING",
                    "DOC-" + d.getId() + "-" + d.getFechaVencimiento() + (vencido ? "-V" : ""), "/rrhh/vencimientos");
        }
        return n;
    }

    private int stockMinimo() {
        List<Usuario> destino = usuarios("LOGISTICA", "ADMIN");
        int n = 0;
        for (Repuesto r : repuestoRepository.findAll()) {
            if (!Boolean.TRUE.equals(r.getEstado()) || r.getStockMinimo() == null || r.getStockMinimo() <= 0
                    || r.getStockActual() > r.getStockMinimo()) continue;
            n += enviar(destino, "Stock mínimo alcanzado",
                    r.getCodigo() + " - " + r.getNombre() + ": quedan " + r.getStockActual() + " (mínimo " + r.getStockMinimo() + ")",
                    r.getStockActual() == 0 ? "ERROR" : "WARNING",
                    "STOCK-" + r.getId() + "-" + r.getStockActual(), "/logistica/repuestos");
        }
        return n;
    }

    private int planesPreventivos() {
        List<Usuario> destino = usuarios("OPERACIONES", "ADMIN");
        int n = 0;
        for (PlanMantenimiento p : planRepository.findByActivoTrue()) {
            String situacion = p.getSituacion();
            if ("OK".equals(situacion)) continue;
            boolean vencido = "VENCIDO".equals(situacion);
            String limite = p.porFecha() ? p.getProximaFecha().format(FECHA) : p.getProximoValor() + " " + p.getUnidad();
            n += enviar(destino, vencido ? "Mantenimiento preventivo vencido" : "Mantenimiento preventivo próximo",
                    p.getMaquinaria().getCodigoInterno() + " - " + p.getActividad() + ": " + p.getPorcentaje() + "% del intervalo (límite " + limite + ")",
                    vencido ? "ERROR" : "WARNING",
                    "PLAN-" + p.getId() + "-" + situacion + "-" + (p.porFecha() ? p.getProximaFecha() : p.getProximoValor()),
                    "/operaciones/mantenimiento/planes");
        }
        return n;
    }

    private int documentosUnidad(LocalDate hoy) {
        List<Usuario> destino = usuarios("OPERACIONES", "ADMIN");
        int n = 0;
        for (Maquinaria m : maquinariaRepository.findAll()) {
            n += documentoUnidad(destino, m, "SOAT", m.getSoatVencimiento(), hoy);
            n += documentoUnidad(destino, m, "Revisión técnica", m.getRevisionTecnicaVencimiento(), hoy);
        }
        return n;
    }

    private int documentoUnidad(List<Usuario> destino, Maquinaria m, String documento, LocalDate vence, LocalDate hoy) {
        if (vence == null || vence.isAfter(hoy.plusDays(DIAS_DOCUMENTOS_UNIDAD))) return 0;
        boolean vencido = vence.isBefore(hoy);
        return enviar(destino, (vencido ? "Vencido: " : "Por vencer: ") + documento,
                "Unidad " + m.getCodigoInterno() + (m.getPlaca() != null ? " (" + m.getPlaca() + ")" : "")
                        + (vencido ? ": venció el " : ": vence el ") + vence.format(FECHA),
                vencido ? "ERROR" : "WARNING",
                "UNI-" + documento.charAt(0) + "-" + m.getId() + "-" + vence + (vencido ? "-V" : ""), "/operaciones/maquinaria");
    }

    private int contratosPorVencer(LocalDate hoy) {
        List<Usuario> destino = usuarios("VENTAS", "GERENCIA", "ADMIN");
        int n = 0;
        for (Contrato c : contratoRepository.findByEstadoOrderByFechaInicioDesc("VIGENTE")) {
            if (c.getFechaFin() == null || c.getFechaFin().isBefore(hoy)
                    || c.getFechaFin().isAfter(hoy.plusDays(DIAS_CONTRATOS))) continue;
            long dias = ChronoUnit.DAYS.between(hoy, c.getFechaFin());
            n += enviar(destino, "Contrato por vencer",
                    "El contrato " + c.getNumero() + " vence el " + c.getFechaFin().format(FECHA) + " (" + dias + " días)",
                    "INFO", "CTR-" + c.getId() + "-" + c.getFechaFin(), "/ventas/contratos/" + c.getId());
        }
        return n;
    }

    private int serviciosSinAsignar() {
        List<Usuario> destino = usuarios("OPERACIONES", "ADMIN");
        LocalDateTime ahora = LocalDateTime.now();
        int n = 0;
        for (Servicio s : servicioRepository.findAll()) {
            if (!("POR_ASIGNAR".equals(s.getEstado()) || "PROGRAMADO".equals(s.getEstado()))) continue;
            if (s.getFechaProgramada() == null || s.getFechaProgramada().isAfter(ahora.plusHours(HORAS_SERVICIO_SIN_ASIGNAR))) continue;
            boolean sinUnidad = asignacionRepository.findByServicioId(s.getId()).stream()
                    .noneMatch(a -> "ACTIVA".equals(a.getEstado()));
            if (!sinUnidad) continue;
            n += enviar(destino, "Servicio sin asignación",
                    "El servicio #" + s.getId() + " (" + s.getOrigen() + " → " + s.getDestino() + ") inicia el "
                            + s.getFechaProgramada().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) + " y no tiene unidad asignada",
                    "WARNING", "SRV-" + s.getId() + "-" + s.getFechaProgramada(), "/operaciones/asignaciones");
        }
        return n;
    }

    private List<Usuario> usuarios(String... roles) {
        return usuarioRepository.findActivosPorRoles(List.of(roles));
    }

    private int enviar(List<Usuario> destino, String titulo, String mensaje, String tipo, String clave, String enlace) {
        int n = 0;
        for (Usuario u : destino) {
            if (notificacionService.notificar(u, titulo, mensaje, tipo, clave, enlace)) n++;
        }
        return n;
    }
}
