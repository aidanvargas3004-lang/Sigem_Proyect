package com.transur.sigem.maquinaria.service;

import com.transur.sigem.maquinaria.dto.TipoMaquinariaRequest;
import com.transur.sigem.maquinaria.entity.TipoMaquinaria;
import com.transur.sigem.maquinaria.repository.TipoMaquinariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TipoMaquinariaService {

    private final TipoMaquinariaRepository repository;

    @Transactional
    public TipoMaquinaria crear(TipoMaquinariaRequest request) {
        if (repository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe un tipo de maquinaria con ese nombre");
        }
        TipoMaquinaria tipo = new TipoMaquinaria();
        tipo.setNombre(request.getNombre());
        tipo.setDescripcion(request.getDescripcion());
        tipo.setImageUrl(request.getImageUrl());
        return repository.save(tipo);
    }

    @Transactional
    public TipoMaquinaria guardar(TipoMaquinaria tipo) {
        return repository.save(tipo);
    }

    public TipoMaquinaria obtener(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Tipo de maquinaria no encontrado: " + id));
    }

    public List<TipoMaquinaria> listar() {
        return repository.findAll();
    }
}