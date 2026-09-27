package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Almacen;
import com.transmaqsur.sigem.model.Inventario;
import com.transmaqsur.sigem.model.MovimientoInventario;
import com.transmaqsur.sigem.model.Repuesto;
import com.transmaqsur.sigem.model.enums.TipoMovimiento;
import com.transmaqsur.sigem.repository.AlmacenRepository;
import com.transmaqsur.sigem.repository.InventarioRepository;
import com.transmaqsur.sigem.repository.MovimientoInventarioRepository;
import com.transmaqsur.sigem.repository.RepuestoRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/** Almacenes, catálogo de repuestos, stock por almacén y kardex. */
@Service
@RequiredArgsConstructor
@Transactional
public class InventarioService {

    private final AlmacenRepository almacenRepository;
    private final RepuestoRepository repuestoRepository;
    private final InventarioRepository inventarioRepository;
    private final MovimientoInventarioRepository movimientoRepository;
    private final AlertaService alertaService;

    // ------------------------------------------------------------ Almacenes

    @Transactional(readOnly = true)
    public List<Almacen> listarAlmacenes() {
        return almacenRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Almacen> listarAlmacenesActivos() {
        return almacenRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Almacen obtenerAlmacen(Long id) {
        return almacenRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Almacén", id));
    }

    public Almacen guardarAlmacen(Almacen form) {
        if (form.isNuevo()) {
            return almacenRepository.save(form);
        }
        Almacen a = obtenerAlmacen(form.getId());
        Entidades.copiar(form, a);
        return a;
    }

    // ------------------------------------------------------------ Repuestos

    @Transactional(readOnly = true)
    public List<Repuesto> listarRepuestos() {
        return repuestoRepository.findAllByOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Repuesto> listarRepuestosActivos() {
        return repuestoRepository.findByActivoTrueOrderByNombreAsc();
    }

    @Transactional(readOnly = true)
    public Repuesto obtenerRepuesto(Long id) {
        return repuestoRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Repuesto", id));
    }

    public Repuesto guardarRepuesto(Repuesto form) {
        form.setCodigo(form.getCodigo().trim().toUpperCase());
        Long id = form.isNuevo() ? 0L : form.getId();
        if (repuestoRepository.existsByCodigoAndIdNot(form.getCodigo(), id)) {
            throw new NegocioException("Ya existe un repuesto con el código " + form.getCodigo());
        }
        if (form.isNuevo()) {
            return repuestoRepository.save(form);
        }
        Repuesto r = obtenerRepuesto(form.getId());
        Entidades.copiar(form, r);
        return r;
    }

    // ------------------------------------------------------------ Stock

    @Transactional(readOnly = true)
    public BigDecimal stockTotal(Long repuestoId) {
        return inventarioRepository.stockTotal(repuestoId);
    }

    @Transactional(readOnly = true)
    public BigDecimal stock(Long almacenId, Long repuestoId) {
        return inventarioRepository.findByAlmacenIdAndRepuestoId(almacenId, repuestoId)
                .map(Inventario::getCantidad).orElse(BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<Inventario> inventarioCompleto() {
        return inventarioRepository.findAllByOrderByAlmacenNombreAscRepuestoNombreAsc();
    }

    @Transactional(readOnly = true)
    public List<Inventario> inventarioPorAlmacen(Long almacenId) {
        return inventarioRepository.findByAlmacenIdOrderByRepuestoNombreAsc(almacenId);
    }

    @Transactional(readOnly = true)
    public List<Inventario> inventarioPorRepuesto(Long repuestoId) {
        return inventarioRepository.findByRepuestoIdOrderByAlmacenNombreAsc(repuestoId);
    }

    @Transactional(readOnly = true)
    public List<Inventario> conStock() {
        return inventarioRepository.findConStock();
    }

    @Transactional(readOnly = true)
    public List<Inventario> bajoMinimo() {
        return inventarioRepository.findBajoMinimo();
    }

    @Transactional(readOnly = true)
    public Page<MovimientoInventario> movimientos(Long almacenId, Long repuestoId, Pageable pageable) {
        return movimientoRepository.buscar(almacenId, repuestoId, pageable);
    }

    public void entrada(Almacen almacen, Repuesto repuesto, BigDecimal cantidad, String referencia, String observacion) {
        validarCantidad(cantidad);
        Inventario inv = obtenerOCrear(almacen, repuesto);
        inv.setCantidad(inv.getCantidad().add(cantidad));
        registrarMovimiento(TipoMovimiento.ENTRADA, inv, cantidad, referencia, observacion);
    }

    public void salida(Almacen almacen, Repuesto repuesto, BigDecimal cantidad, String referencia, String observacion) {
        validarCantidad(cantidad);
        Inventario inv = obtenerOCrear(almacen, repuesto);
        if (inv.getCantidad().compareTo(cantidad) < 0) {
            throw new NegocioException("Stock insuficiente de " + repuesto.getNombre() + " en " + almacen.getNombre()
                    + ": disponible " + inv.getCantidad().stripTrailingZeros().toPlainString() + " " + repuesto.getUnidadMedida());
        }
        inv.setCantidad(inv.getCantidad().subtract(cantidad));
        registrarMovimiento(TipoMovimiento.SALIDA, inv, cantidad, referencia, observacion);
        alertaService.revisarStock(inv);
    }

    /** Ajuste por inventario físico: fija la cantidad real y registra la diferencia. */
    public void ajustar(Long almacenId, Long repuestoId, BigDecimal cantidadReal, String motivo) {
        if (cantidadReal == null || cantidadReal.signum() < 0) {
            throw new NegocioException("La cantidad real debe ser mayor o igual a cero");
        }
        if (motivo == null || motivo.isBlank()) {
            throw new NegocioException("Indique el motivo del ajuste");
        }
        Inventario inv = obtenerOCrear(obtenerAlmacen(almacenId), obtenerRepuesto(repuestoId));
        BigDecimal diferencia = cantidadReal.subtract(inv.getCantidad());
        if (diferencia.signum() == 0) {
            throw new NegocioException("La cantidad real es igual al stock registrado; no hay nada que ajustar");
        }
        inv.setCantidad(cantidadReal);
        registrarMovimiento(diferencia.signum() > 0 ? TipoMovimiento.AJUSTE_POSITIVO : TipoMovimiento.AJUSTE_NEGATIVO,
                inv, diferencia.abs(), "AJUSTE", motivo);
        alertaService.revisarStock(inv);
    }

    public void transferir(Long origenId, Long destinoId, Long repuestoId, BigDecimal cantidad) {
        if (origenId.equals(destinoId)) {
            throw new NegocioException("El almacén de origen y destino deben ser distintos");
        }
        Almacen origen = obtenerAlmacen(origenId);
        Almacen destino = obtenerAlmacen(destinoId);
        Repuesto r = obtenerRepuesto(repuestoId);
        salida(origen, r, cantidad, "TRANSFERENCIA", "Hacia " + destino.getNombre());
        entrada(destino, r, cantidad, "TRANSFERENCIA", "Desde " + origen.getNombre());
    }

    private Inventario obtenerOCrear(Almacen almacen, Repuesto repuesto) {
        return inventarioRepository.findByAlmacenIdAndRepuestoId(almacen.getId(), repuesto.getId())
                .orElseGet(() -> {
                    Inventario nuevo = new Inventario();
                    nuevo.setAlmacen(almacen);
                    nuevo.setRepuesto(repuesto);
                    return inventarioRepository.save(nuevo);
                });
    }

    private void registrarMovimiento(TipoMovimiento tipo, Inventario inv, BigDecimal cantidad, String referencia, String obs) {
        MovimientoInventario m = new MovimientoInventario();
        m.setTipo(tipo);
        m.setAlmacen(inv.getAlmacen());
        m.setRepuesto(inv.getRepuesto());
        m.setCantidad(cantidad);
        m.setStockResultante(inv.getCantidad());
        m.setReferencia(referencia);
        m.setObservacion(obs);
        movimientoRepository.save(m);
    }

    private void validarCantidad(BigDecimal cantidad) {
        if (cantidad == null || cantidad.signum() <= 0) {
            throw new NegocioException("La cantidad debe ser mayor que cero");
        }
    }
}
