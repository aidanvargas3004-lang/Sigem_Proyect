package com.transur.sigem.sistema.repository;

import com.transur.sigem.sistema.entity.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long> {
    List<Auditoria> findByModuloOrderByFechaDesc(String modulo);
    List<Auditoria> findByUsuarioIdOrderByFechaDesc(Long usuarioId);
}