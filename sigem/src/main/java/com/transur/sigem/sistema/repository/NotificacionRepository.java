package com.transur.sigem.sistema.repository;

import com.transur.sigem.sistema.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {
    List<Notificacion> findByUsuarioIdAndLeidaFalseOrderByFechaDesc(Long usuarioId);
    long countByUsuarioIdAndLeidaFalse(Long usuarioId); // Para el badge rojo del navbar
    List<Notificacion> findTop100ByUsuarioIdOrderByFechaDesc(Long usuarioId);
    /** Evita generar dos veces la misma alerta automática para un usuario. */
    boolean existsByUsuarioIdAndClave(Long usuarioId, String clave);
}
