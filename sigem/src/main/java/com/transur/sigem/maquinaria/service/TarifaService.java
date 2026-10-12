package com.transur.sigem.maquinaria.service;

import com.transur.sigem.maquinaria.dto.TarifaRequest;
import com.transur.sigem.maquinaria.dto.TarifaResponse;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.Tarifa;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class TarifaService {

    private final TarifaRepository tarifaRepository;
    private final MaquinariaRepository maquinariaRepository;

    @Transactional
    public TarifaResponse crear(TarifaRequest request) {
        Maquinaria maq = maquinariaRepository.findById(request.getMaquinariaId())
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada con ID: " + request.getMaquinariaId()));

        Tarifa tarifa = new Tarifa();
        tarifa.setMaquinaria(maq);
        tarifa.setTipoTarifa(request.getTipoTarifa());
        tarifa.setPrecio(request.getPrecio());
        tarifa.setVigenciaInicio(request.getVigenciaInicio());
        tarifa.setVigenciaFin(request.getVigenciaFin());

        return mapToResponse(tarifaRepository.save(tarifa));
    }

    public List<TarifaResponse> listarPorMaquinaria(Long maquinariaId) {
        return tarifaRepository.findByMaquinariaId(maquinariaId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ✅ MÉTODO FALTANTE AGREGADO AQUÍ
    private TarifaResponse mapToResponse(Tarifa t) {
        return TarifaResponse.builder()
                .id(t.getId())
                .maquinariaCodigo(t.getMaquinaria().getCodigoInterno())
                .tipoTarifa(t.getTipoTarifa())
                .precio(t.getPrecio())
                .vigenciaInicio(t.getVigenciaInicio())
                .vigenciaFin(t.getVigenciaFin())
                .build();
    }

    public List<Tarifa> listarTodas() {
        return tarifaRepository.findAll();
    }
}