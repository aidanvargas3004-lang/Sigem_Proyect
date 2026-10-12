package com.transur.sigem.marketing.repository;

import com.transur.sigem.marketing.entity.Lead;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LeadRepository extends JpaRepository<Lead, Long> {

    // ✅ ESTE ES EL MÉTODO QUE FALTA (Línea 48 del Service)
    List<Lead> findByCampaniaId(Long campaniaId);

    // Los otros métodos que ya agregamos antes
    long countByEstado(String estado);
    List<Lead> findTop5ByOrderByFechaRegistroDesc();

    List<Lead> findByEmpresaContainingIgnoreCaseOrNombresContainingIgnoreCaseOrTelefonoContaining(
            String empresa, String nombres, String telefono
    );

    @Query("SELECT COUNT(l) FROM Lead l WHERE YEAR(l.fechaRegistro) = :anio AND MONTH(l.fechaRegistro) = :mes")
    int countByFechaRegistroYearAndMonth(@Param("anio") int anio, @Param("mes") int mes);
}