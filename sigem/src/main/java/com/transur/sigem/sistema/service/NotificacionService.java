package com.transur.sigem.sistema.service;

import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.dto.NotificacionResponse;
import com.transur.sigem.sistema.entity.Notificacion;
import com.transur.sigem.sistema.repository.NotificacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificacionService {

    private final NotificacionRepository notifRepository;
    private final UsuarioRepository usuarioRepository; // ✅ Inyectado para asignar usuarios

    /**
     * Registra una alerta automática para un usuario, salvo que ya exista una con la misma clave.
     * Las reglas que deciden qué alertar viven en {@link AlertaService} (SRP).
     */
    @Transactional
    public boolean notificar(Usuario usuario, String titulo, String mensaje, String tipo, String clave, String enlace) {
        if (clave != null && notifRepository.existsByUsuarioIdAndClave(usuario.getId(), clave)) {
            return false;
        }
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setTitulo(titulo);
        n.setMensaje(mensaje);
        n.setTipo(tipo);
        n.setClave(clave);
        n.setEnlace(enlace);
        n.setLeida(false);
        n.setFecha(LocalDateTime.now());
        notifRepository.save(n);
        return true;
    }

    public List<NotificacionResponse> listarRecientes(Long usuarioId) {
        return notifRepository.findTop100ByUsuarioIdOrderByFechaDesc(usuarioId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /** Marca como leída solo si la notificación pertenece al usuario; devuelve su enlace. */
    @Transactional
    public String marcarLeidaDeUsuario(Long notificacionId, Long usuarioId) {
        Notificacion n = notifRepository.findById(notificacionId)
                .filter(x -> x.getUsuario() != null && x.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", notificacionId));
        n.setLeida(true);
        return n.getEnlace();
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

    public List<NotificacionResponse> listarNoLeidas(Long usuarioId) {
        return notifRepository.findByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId).stream()
                .map(this::mapToResponse) // ✅ Usar mapper unificado
                .collect(Collectors.toList());
    }

    @Transactional
    public void marcarComoLeida(Long notificacionId) {
        Notificacion n = notifRepository.findById(notificacionId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Notificación", notificacionId));
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
                .enlace(n.getEnlace())
                .build();
    }
}