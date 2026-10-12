package com.transur.sigem.logistica.service;

import com.transur.sigem.logistica.dto.RepuestoRequest;
import com.transur.sigem.logistica.entity.CategoriaRepuesto;
import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.CategoriaRepuestoRepository;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class RepuestoService {

    private final RepuestoRepository repuestoRepository;
    private final CategoriaRepuestoRepository categoriaRepository;

    @Transactional
    public Repuesto crear(RepuestoRequest request) {
        if (repuestoRepository.existsByCodigo(request.getCodigo())) {
            throw new RuntimeException("Ya existe un repuesto con ese código SKU");
        }

        CategoriaRepuesto categoria = categoriaRepository.findById(request.getCategoriaId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada"));

        Repuesto repuesto = new Repuesto();
        repuesto.setCodigo(request.getCodigo());
        repuesto.setNombre(request.getNombre());
        repuesto.setDescripcion(request.getDescripcion());
        repuesto.setCategoria(categoria);
        repuesto.setStockActual(request.getStockActual() != null ? request.getStockActual() : 0);
        repuesto.setStockMinimo(request.getStockMinimo() != null ? request.getStockMinimo() : 0);
        repuesto.setUnidadMedida(request.getUnidadMedida());
        repuesto.setEstado(true);

        return repuestoRepository.save(repuesto);
    }

    public List<Repuesto> listar() {
        List<Repuesto> repuestos = repuestoRepository.findAll();
        repuestos.forEach(r -> {
            if (r.getCategoria() != null) r.getCategoria().getNombre();
        });
        return repuestos;
    }

    public Repuesto obtenerPorId(Long id) {
        return repuestoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Repuesto no encontrado"));
    }
}