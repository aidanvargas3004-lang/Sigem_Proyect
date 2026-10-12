package com.transur.sigem.security.repository;

import com.transur.sigem.security.entity.Permiso;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {
}