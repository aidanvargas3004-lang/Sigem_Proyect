package com.transur.sigem.activos.repository;

import com.transur.sigem.activos.entity.AdquisicionMaquinaria;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdquisicionRepository extends JpaRepository<AdquisicionMaquinaria, Long> {
    // Puedes agregar búsquedas por proveedor o tipo si lo necesitas
}