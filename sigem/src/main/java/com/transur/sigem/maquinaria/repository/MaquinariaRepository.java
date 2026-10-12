package com.transur.sigem.maquinaria.repository;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MaquinariaRepository extends JpaRepository<Maquinaria, Long> {
    Optional<Maquinaria> findByCodigoInterno(String codigoInterno);
    boolean existsByCodigoInterno(String codigoInterno);
    boolean existsByPlaca(String placa);
    List<Maquinaria> findByEstadoOperativo(String estado);
}