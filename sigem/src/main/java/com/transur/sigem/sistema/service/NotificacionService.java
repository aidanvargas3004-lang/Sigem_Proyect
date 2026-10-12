package com.transur.sigem.sistema.service;

import com.transur.sigem.rrhh.entity.DocumentoEmpleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.dto.NotificacionResponse;
import com.transur.sigem.sistema.entity.Notificacion;
import com.transur.sigem.sistema.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionService {

    private final NotificacionRepository notifRepository;
    private final DocumentoEmpleadoRepository docEmpleadoRepo;
    private final RepuestoRepository repuestoRepo;
    private final UsuarioRepository usuarioRepository; // ✅ Inyectado para asignar usuarios

    // ✅ ALERTAS AUTOMÁTICAS DIARIAS (8:00 AM)
    @Scheduled(cron = "0 0 8 * * ?")
    @Transactional
    public void generarAlertasAutomaticas() {
        LocalDate hoy = LocalDate.now();
        LocalDate en30Dias = hoy.plusDays(30);

        // 1. Alerta por documentos próximos a vencer (RRHH)
        List<DocumentoEmpleado> docsVenciendo = docEmpleadoRepo
                .findByFechaVencimientoBetweenAndEstadoTrue(hoy, en30Dias);

        for (DocumentoEmpleado doc : docsVenciendo) {
            Long userId = null;
            if (doc.getEmpleado() != null && doc.getEmpleado().getUsuario() != null) {
                userId = doc.getEmpleado().getUsuario().getId();
            }

            crearNotificacion(
                    userId,
                    "VENCIMIENTO DE DOCUMENTO",
                    "La licencia/certificado " + doc.getTipoDocumento() + " de " +
                            doc.getEmpleado().getNombres() + " vence el " + doc.getFechaVencimiento(),
                    "WARNING"
            );
        }

        // 2. Alerta por stock mínimo alcanzado (Logística)
        List<Repuesto> stockBajo = repuestoRepo.findAll().stream()
                .filter(r -> r.getStockActual() <= r.getStockMinimo() && r.getStockMinimo() > 0)
                .collect(Collectors.toList());

        for (Repuesto rep : stockBajo) {
            // Notificar a todos los usuarios activos (o filtra por rol si prefieres)
            List<Usuario> usuarios = usuarioRepository.findByEstadoTrue();
            for (Usuario u : usuarios) {
                crearNotificacion(
                        u.getId(),
                        "STOCK MÍNIMO ALCANZADO",
                        "El repuesto " + rep.getCodigo() + " (" + rep.getNombre() +
                                ") tiene solo " + rep.getStockActual() + " unidades disponibles.",
                        "ERROR"
                );
            }
        }
    }

    // ✅ CORREGIDO: Ahora devuelve DTO directamente
    @Transactional
    public NotificacionResponse crearManual(Long usuarioId, String titulo, String mensaje, String tipo) {
        Notificacion saved = crearNotificacion(usuarioId, titulo, mensaje, tipo);
        return mapToResponse(saved);
    }

    private Notificacion crearNotificacion(Long usuarioId, String titulo, String mensaje, String tipo) {
        Notificacion n = new Notificacion();

        // ✅ ASIGNAR USUARIO CORRECTAMENTE
        if (usuarioId != null) {
            Usuario usuario = usuarioRepository.findById(usuarioId).orElse(null);
            n.setUsuario(usuario);
        }

        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setLeida(false);
        n.setFecha(LocalDateTime.now());

        return notifRepository.save(n);
    }

    private void crearNotificacionGlobal(String titulo, String mensaje, String tipo) {
        crearNotificacion(null, titulo, mensaje, tipo);
    }

    public List<NotificacionResponse> listarNoLeidas(Long usuarioId) {
        return notifRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId).stream()
                .map(this::mapToResponse) // ✅ Usar mapper unificado
                .collect(Collectors.toList());
    }

    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        Notificacion n = notifRepository.findById(notificacionId)
                .orElseThrow(() -> new RuntimeException("Notificación no encontrada"));
        n.setLeida(true);
        notifRepository.save(n);
    }

    public long contarNoLeidas(Long usuarioId) {
        return notifRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional
    public void marcarTodasComoLeidas(Long usuarioId) {
        List<Notificacion> noLeidas = notifRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId);
        noLeidas.forEach(n -> n.setLeida(true));
        notifRepository.saveAll(noLeidas);
    }

    // ✅ MAPPER PRIVADO UNIFICADO
    private NotificacionResponse mapToResponse(Notificacion n) {
        return NotificacionResponse.builder()
                .id(n.getId())
                .titulo(n.getTitulo())
                .mensaje(n.getMensaje())
                .tipo(n.getTipo())
                .leida(n.getLeida())
                .fecha(n.getFecha())
                .build();
    }
}