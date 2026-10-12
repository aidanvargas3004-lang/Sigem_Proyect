package com.transur.sigem.logistica.service;

import com.transur.sigem.logistica.dto.MovimientoRequest;
import com.transur.sigem.logistica.entity.MovimientoInventario;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.entity.UbicacionAlmacen;
import com.transur.sigem.logistica.repository.MovimientoInventarioRepository;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.logistica.repository.UbicacionAlmacenRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MovimientoInventarioService {

    private final MovimientoInventarioRepository movRepository;
    private final RepuestoRepository repuestoRepository;
    private final UbicacionAlmacenRepository ubicacionRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public MovimientoInventario registrar(MovimientoRequest request) {
        Repuesto repuesto = repuestoRepository.findById(request.getRepuestoId())
                .orElseThrow(() -> new com.transur.sigem.common.exception.RecursoNoEncontradoException("Repuesto", request.getRepuestoId()));

        // Validar stock suficiente para SALIDAS
        if ("SALIDA".equals(request.getTipoMovimiento()) &&
                repuesto.getStockActual() < request.getCantidad()) {
            throw new com.transur.sigem.common.exception.NegocioException("Stock insuficiente de " + repuesto.getNombre() + ". Actual: " + repuesto.getStockActual());
        }

        // Actualizar stock según tipo de movimiento
        switch (request.getTipoMovimiento()) {
            case "ENTRADA":
            case "DEVOLUCION":
            case "AJUSTE_POSITIVO":
                repuesto.setStockActual(repuesto.getStockActual() + request.getCantidad());
                break;
            case "SALIDA":
            case "AJUSTE_NEGATIVO":
                repuesto.setStockActual(repuesto.getStockActual() - request.getCantidad());
                break;
            default:
                throw new RuntimeException("Tipo de movimiento no válido: " + request.getTipoMovimiento());
        }

        repuestoRepository.save(repuesto); // Persistir cambio de stock inmediatamente

        MovimientoInventario mov = new MovimientoInventario();
        mov.setRepuesto(repuesto);

        if (request.getUbicacionId() != null) {
            UbicacionAlmacen ubi = ubicacionRepository.findById(request.getUbicacionId())
                    .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));
            mov.setUbicacion(ubi);
        }

        mov.setTipoMovimiento(request.getTipoMovimiento());
        mov.setCantidad(request.getCantidad());
        mov.setMotivo(request.getMotivo());
        mov.setFecha(LocalDateTime.now());

        if (request.getUsuarioId() != null) {
            Usuario u = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            mov.setUsuario(u);
        }

        return movRepository.save(mov);
    }

    public List<MovimientoInventario> historialPorRepuesto(Long repuestoId) {
        List<MovimientoInventario> movimientos = movRepository.findByRepuestoIdOrderByFechaDesc(repuestoId);
        inicializarRelaciones(movimientos);
        return movimientos;
    }

    public List<MovimientoInventario> listarTodos() {
        List<MovimientoInventario> movimientos = movRepository.findAllByOrderByFechaDesc();
        inicializarRelaciones(movimientos);
        return movimientos;
    }

    private void inicializarRelaciones(List<MovimientoInventario> movimientos) {
        movimientos.forEach(m -> {
            if (m.getRepuesto() != null) m.getRepuesto().getNombre();
            if (m.getUbicacion() != null) {
                m.getUbicacion().getCodigo();
                if (m.getUbicacion().getAlmacen() != null) m.getUbicacion().getAlmacen().getNombre();
            }
            if (m.getUsuario() != null) m.getUsuario().getUsername();
        });
    }
}