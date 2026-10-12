package com.transur.sigem.marketing.repository;

import com.transur.sigem.marketing.entity.Campania;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CampaniaRepository extends JpaRepository<Campania, Long> {
    long countByEstado(String estado);

    // Otros métodos útiles que probablemente necesites
    List<Campania> findByEstado(String estado);
}