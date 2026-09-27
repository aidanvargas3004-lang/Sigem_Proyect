package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Empleado;
import com.transmaqsur.sigem.model.Notificacion;
import com.transmaqsur.sigem.model.Usuario;
import com.transmaqsur.sigem.model.enums.TipoNotificacion;
import com.transmaqsur.sigem.repository.NotificacionRepository;
import com.transmaqsur.sigem.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificacionService {

    private final NotificacionRepository notificacionRepository;
    private final UsuarioRepository usuarioRepository;

    /**
     * Envía una notificación a un usuario. Si se indica una clave, no se repite
     * una notificación con la misma clave (evita alertas duplicadas).
     */
    public void notificar(Usuario usuario, String titulo, String mensaje, TipoNotificacion tipo, String enlace, String clave) {
        if (clave != null && notificacionRepository.existsByUsuarioIdAndClave(usuario.getId(), clave)) {
            return;
        }
        Notificacion n = new Notificacion();
        n.setUsuario(usuario);
        n.setTitulo(titulo);
        n.setMensaje(mensaje.length() > 500 ? mensaje.substring(0, 497) + "..." : mensaje);
        n.setTipo(tipo);
        n.setEnlace(enlace);
        n.setClave(clave);
        notificacionRepository.save(n);
    }

    /** Notifica a todos los usuarios activos que tienen un permiso (p. ej. MANTENIMIENTO_GESTIONAR). */
    public void notificarPermiso(String permiso, String titulo, String mensaje, TipoNotificacion tipo, String enlace, String clave) {
        for (Usuario u : usuarioRepository.findActivosConPermiso(permiso)) {
            notificar(u, titulo, mensaje, tipo, enlace, clave);
        }
    }

    /** Notifica a los usuarios vinculados a un empleado (conductor, operador, técnico). */
    public void notificarEmpleado(Empleado empleado, String titulo, String mensaje, TipoNotificacion tipo, String enlace, String clave) {
        if (empleado == null) {
            return;
        }
        for (Usuario u : usuarioRepository.findByEmpleadoIdAndActivoTrue(empleado.getId())) {
            notificar(u, titulo, mensaje, tipo, enlace, clave);
        }
    }

    @Transactional(readOnly = true)
    public long contarNoLeidas(Long usuarioId) {
        return notificacionRepository.countByUsuarioIdAndLeidaFalse(usuarioId);
    }

    @Transactional(readOnly = true)
    public List<Notificacion> recientes(Long usuarioId) {
        return notificacionRepository.findTop5ByUsuarioIdAndLeidaFalseOrderByFechaDesc(usuarioId);
    }

    @Transactional(readOnly = true)
    public Page<Notificacion> listar(Long usuarioId, Pageable pageable) {
        return notificacionRepository.findByUsuarioIdOrderByFechaDesc(usuarioId, pageable);
    }

    /** Marca como leída y devuelve el enlace de destino. */
    public String marcarLeida(Long id, Long usuarioId) {
        Notificacion n = notificacionRepository.findById(id)
                .filter(x -> x.getUsuario().getId().equals(usuarioId))
                .orElseThrow(() -> new NoEncontradoException("Notificación", id));
        n.setLeida(true);
        return n.getEnlace();
    }

    public void marcarTodasLeidas(Long usuarioId) {
        notificacionRepository.marcarTodasLeidas(usuarioId);
    }
}
