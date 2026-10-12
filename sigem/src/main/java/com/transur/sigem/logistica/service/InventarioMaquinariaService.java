package com.transur.sigem.logistica.service;

import com.transur.sigem.logistica.dto.InventarioMaquinariaRequest;
import com.transur.sigem.logistica.entity.InventarioMaquinaria;
import com.transur.sigem.logistica.entity.UbicacionAlmacen;
import com.transur.sigem.logistica.repository.InventarioMaquinariaRepository;
import com.transur.sigem.logistica.repository.UbicacionAlmacenRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InventarioMaquinariaService {

    private final InventarioMaquinariaRepository invRepository;
    private final MaquinariaRepository maqRepository;
    private final UbicacionAlmacenRepository ubiRepository;

    @Transactional
    public InventarioMaquinaria registrar(InventarioMaquinariaRequest request) {
        Maquinaria maq = maqRepository.findById(request.getMaquinariaId())
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

        // Verificar si ya tiene registro físico (relación 1:1)
        if (invRepository.findByMaquinariaId(request.getMaquinariaId()).isPresent()) {
            throw new RuntimeException("Esta maquinaria ya tiene un registro de inventario físico asignado");
        }

        InventarioMaquinaria inv = new InventarioMaquinaria();
        inv.setMaquinaria(maq);

        if (request.getUbicacionId() != null) {
            UbicacionAlmacen ubi = ubiRepository.findById(request.getUbicacionId())
                    .orElseThrow(() -> new RuntimeException("Ubicación no encontrada"));
            inv.setUbicacion(ubi);
        }

        inv.setSituacionLogistica(request.getSituacionLogistica());
        inv.setFechaIngreso(request.getFechaIngreso() != null ? request.getFechaIngreso() : LocalDate.now());
        inv.setObservaciones(request.getObservaciones());

        return invRepository.save(inv);
    }

    public List<InventarioMaquinaria> listar() {
        List<InventarioMaquinaria> inventario = invRepository.findAll();
        inventario.forEach(i -> {
            if (i.getMaquinaria() != null) {
                i.getMaquinaria().getCodigoInterno();
                if (i.getMaquinaria().getTipo() != null) i.getMaquinaria().getTipo().getNombre();
            }
            if (i.getUbicacion() != null) {
                i.getUbicacion().getCodigo();
                if (i.getUbicacion().getAlmacen() != null) i.getUbicacion().getAlmacen().getNombre();
            }
        });
        return inventario;
    }
}