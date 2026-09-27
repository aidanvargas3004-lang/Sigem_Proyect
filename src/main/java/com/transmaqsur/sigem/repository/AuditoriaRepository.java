package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Auditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AuditoriaRepository extends JpaRepository<Auditoria, Long>, JpaSpecificationExecutor<Auditoria> {

    List<Auditoria> findByEntidadAndEntidadIdOrderByFechaDesc(String entidad, Long entidadId);

    @Query("select distinct a.entidad from Auditoria a order by a.entidad")
    List<String> entidades();
}
