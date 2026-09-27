package com.transmaqsur.sigem.repository;

import com.transmaqsur.sigem.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;
import com.transmaqsur.sigem.model.enums.EstadoVenta;
import com.transmaqsur.sigem.model.enums.TipoComprobante;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.List;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    List<Venta> findAllByOrderByIdDesc();

    List<Venta> findByClienteIdOrderByIdDesc(Long clienteId);

    @Query("select coalesce(max(v.numero), 0) from Venta v where v.tipoComprobante = :tipo")
    int ultimoNumero(TipoComprobante tipo);

    @Query("select coalesce(sum(v.total), 0) from Venta v where v.estado in :estados and v.fechaEmision between :desde and :hasta")
    BigDecimal totalEntre(LocalDate desde, LocalDate hasta, Collection<EstadoVenta> estados);

    List<Venta> findByEstadoInAndFechaEmisionBetweenOrderByFechaEmisionAsc(Collection<EstadoVenta> estados, LocalDate desde, LocalDate hasta);
}
