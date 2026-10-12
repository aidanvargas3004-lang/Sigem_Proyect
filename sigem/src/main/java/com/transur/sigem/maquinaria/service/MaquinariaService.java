package com.transur.sigem.maquinaria.service;

import com.transur.sigem.maquinaria.dto.MaquinariaRequest;
import com.transur.sigem.maquinaria.dto.MaquinariaResponse;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.TipoMaquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TipoMaquinariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MaquinariaService {

    private final MaquinariaRepository maquinariaRepository;
    private final TipoMaquinariaRepository tipoRepository;

    //crear
    @Transactional
    public MaquinariaResponse crear(MaquinariaRequest request) {
        if (maquinariaRepository.existsByCodigoInterno(request.getCodigoInterno())) {
            throw new RuntimeException("Ya existe maquinaria con código: " + request.getCodigoInterno());
        }

        TipoMaquinaria tipo = tipoRepository.findById(request.getTipoId())
                .orElseThrow(() -> new RuntimeException("Tipo de maquinaria no encontrado con ID: " + request.getTipoId()));

        Maquinaria maq = new Maquinaria();
        maq.setCodigoInterno(request.getCodigoInterno());
        maq.setTipo(tipo);
        maq.setMarca(request.getMarca());
        maq.setModelo(request.getModelo());
        maq.setAnio(request.getAnio());
        maq.setPlaca(request.getPlaca());
        maq.setHorometroActual(request.getHorometroActual() != null ? request.getHorometroActual() : BigDecimal.ZERO);
        maq.setEstadoOperativo(request.getEstadoOperativo() != null ? request.getEstadoOperativo() : "OPERATIVO");
        maq.setDestinoOperativo(request.getDestinoOperativo() != null ? request.getDestinoOperativo() : "MULTIPROPOSITO");
        maq.setPrecioBaseAlquiler(request.getPrecioBaseAlquiler());
        maq.setPrecioBaseVenta(request.getPrecioBaseVenta());
        maq.setImageUrl(request.getImageUrl());

        return mapToResponse(maquinariaRepository.save(maq));
    }

    //  LISTAR TODOS
    public List<MaquinariaResponse> listar() {
        return maquinariaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    //  OBTENER POR ID
    public MaquinariaResponse obtenerPorId(Long id) {
        Maquinaria maq = maquinariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada con ID: " + id));
        return mapToResponse(maq);
    }

    // ✅ ACTUALIZAR
    @Transactional
    public MaquinariaResponse actualizar(Long id, MaquinariaRequest request) {
        Maquinaria maq = maquinariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

        if (!maq.getCodigoInterno().equals(request.getCodigoInterno()) &&
                maquinariaRepository.existsByCodigoInterno(request.getCodigoInterno())) {
            throw new RuntimeException("Código interno ya registrado en otra máquina");
        }

        TipoMaquinaria tipo = tipoRepository.findById(request.getTipoId())
                .orElseThrow(() -> new RuntimeException("Tipo de maquinaria no encontrado"));

        maq.setCodigoInterno(request.getCodigoInterno());
        maq.setTipo(tipo);
        maq.setMarca(request.getMarca());
        maq.setModelo(request.getModelo());
        maq.setAnio(request.getAnio());
        maq.setPlaca(request.getPlaca());
        maq.setHorometroActual(request.getHorometroActual());
        maq.setEstadoOperativo(request.getEstadoOperativo());
        maq.setDestinoOperativo(request.getDestinoOperativo());
        maq.setPrecioBaseAlquiler(request.getPrecioBaseAlquiler());
        maq.setPrecioBaseVenta(request.getPrecioBaseVenta());
        maq.setImageUrl(request.getImageUrl());

        return mapToResponse(maquinariaRepository.save(maq));
    }

    // ✅ MAPPER PRIVADO
    private MaquinariaResponse mapToResponse(Maquinaria m) {
        return MaquinariaResponse.builder()
                .id(m.getId())
                .codigoInterno(m.getCodigoInterno())
                .marca(m.getMarca())
                .modelo(m.getModelo())
                .anio(m.getAnio())
                .tipoNombre(m.getTipo().getNombre())
                .placa(m.getPlaca())
                .horometroActual(m.getHorometroActual())
                .estadoOperativo(m.getEstadoOperativo())
                .destinoOperativo(m.getDestinoOperativo())
                .precioBaseAlquiler(m.getPrecioBaseAlquiler())
                .precioBaseVenta(m.getPrecioBaseVenta())
                .imageUrl(m.getImageUrl())
                .build();
    }
    @Transactional
    public void eliminar(Long id) {
        Maquinaria maquinaria = maquinariaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada: " + id));
        // Las máquinas tienen historial comercial/operativo, por lo que nunca se eliminan físicamente.
        maquinaria.setEstadoOperativo("BAJA_DEFINITIVA");
        maquinariaRepository.save(maquinaria);
    }
    public List<Maquinaria> listarActivos() {
        return maquinariaRepository.findByEstadoOperativo("OPERATIVO");
    }



}