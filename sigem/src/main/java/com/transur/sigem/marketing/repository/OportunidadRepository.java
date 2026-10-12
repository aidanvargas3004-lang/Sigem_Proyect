package com.transur.sigem.marketing.repository;

import com.transur.sigem.marketing.entity.Oportunidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface OportunidadRepository extends JpaRepository<Oportunidad, Long> {

    // Contar por estado (este sí funciona por nombre)
    long countByEstado(String estado);

    // ✅ CORRECCIÓN: Usar @Query para SUMAR valores
    // Asegúrate que 'montoEstimado' sea el nombre exacto del campo en tu entidad
    @Query("SELECT SUM(o.montoEstimado) FROM Oportunidad o WHERE o.estado = :estado")
    BigDecimal sumarValoresPorEstado(@Param("estado") String estado);

    // Listar por estado (para calcular promedios en el service)
    List<Oportunidad> findByEstado(String estado);

    @Query("SELECT SUM(o.montoEstimado) FROM Oportunidad o")
    BigDecimal sumAllMontoEstimado();

    @Query("SELECT COALESCE(SUM(o.montoEstimado), 0) FROM Oportunidad o WHERE YEAR(o.fechaCreacion) = :anio AND MONTH(o.fechaCreacion) = :mes")
    double sumMontoByFechaCreacionYearAndMonth(@Param("anio") int anio, @Param("mes") int mes);

    @Query("SELECT COUNT(o) FROM Oportunidad o WHERE YEAR(o.fechaCreacion) = :anio AND MONTH(o.fechaCreacion) = :mes")
    int countByFechaCreacionYearAndMonth(@Param("anio") int anio, @Param("mes") int mes);
}