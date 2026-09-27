package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface NotificacionRepository extends JpaRepository<Notificacion, Long> {

    long countByUsuarioIdAndLeidaFalse(Long usuarioId);

    List<Notificacion> findTop5ByUsuarioIdAndLeidaFalseOrderByFechaDesc(Long usuarioId);

    Page<Notificacion> findByUsuarioIdOrderByFechaDesc(Long usuarioId, Pageable pageable);

    boolean existsByUsuarioIdAndClave(Long usuarioId, String clave);

    @Modifying
    @Query("update Notificacion n set n.leida = true where n.usuario.id = :usuarioId and n.leida = false")
    int marcarTodasLeidas(Long usuarioId);
}
