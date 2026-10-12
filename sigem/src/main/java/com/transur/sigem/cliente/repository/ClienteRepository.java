package com.transur.sigem.cliente.repository;

import com.transur.sigem.cliente.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
    Optional<Cliente> findByDocumento(String documento);
    Optional<Cliente> findByUsuarioId(Long usuarioId);
    boolean existsByDocumento(String documento);
    boolean existsByEmail(String email);
    List<Cliente> findAllByOrderByRazonSocialAsc();
    @Query("SELECT COUNT(c) FROM Cliente c WHERE YEAR(c.fechaRegistro) = :anio AND MONTH(c.fechaRegistro) = :mes")
    int countByFechaRegistroYearAndMonth(@Param("anio") int anio, @Param("mes") int mes);
}