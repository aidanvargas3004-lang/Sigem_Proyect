package com.transur.sigem.maquinaria.repository;

import com.transur.sigem.maquinaria.entity.Tarifa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface TarifaRepository extends JpaRepository<Tarifa, Long> {
    List<Tarifa> findByMaquinariaId(Long maquinariaId);

    @Query("SELECT t FROM Tarifa t JOIN FETCH t.maquinaria")
    List<Tarifa> findAllWithMaquinaria();
}