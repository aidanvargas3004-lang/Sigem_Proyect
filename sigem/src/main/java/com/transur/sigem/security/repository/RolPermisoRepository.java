package com.transur.sigem.security.repository;

import com.transur.sigem.security.entity.RolPermiso;
import com.transur.sigem.security.entity.RolPermisoId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolPermisoRepository extends JpaRepository<RolPermiso, RolPermisoId> {
}