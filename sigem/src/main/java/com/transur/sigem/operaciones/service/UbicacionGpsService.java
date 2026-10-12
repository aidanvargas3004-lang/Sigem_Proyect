package com.transur.sigem.operaciones.service;

import com.transur.sigem.common.exception.NegocioException;
import com.transur.sigem.common.exception.RecursoNoEncontradoException;
import com.transur.sigem.common.util.UsuarioActual;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.dto.UbicacionGpsRequest;
import com.transur.sigem.operaciones.entity.Servicio;
import com.transur.sigem.operaciones.entity.UbicacionGps;
import com.transur.sigem.operaciones.repository.ServicioRepository;
import com.transur.sigem.operaciones.repository.UbicacionGpsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UbicacionGpsService {

    private final UbicacionGpsRepository ubicacionRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final ServicioRepository servicioRepository;

    @Transactional
    public UbicacionGps registrar(UbicacionGpsRequest r) {
        UbicacionGps g = new UbicacionGps();
        g.setMaquinaria(maquinariaRepository.findById(r.getMaquinariaId())
                .orElseThrow(() -> new RecursoNoEncontradoException("Maquinaria", r.getMaquinariaId())));
        if (r.getServicioId() != null) {
            Servicio s = servicioRepository.findById(r.getServicioId())
                    .orElseThrow(() -> new RecursoNoEncontradoException("Servicio", r.getServicioId()));
            g.setServicio(s);
        }
        if (r.getLatitud() == null || r.getLongitud() == null || Math.abs(r.getLatitud()) > 90 || Math.abs(r.getLongitud()) > 180) {
            throw new NegocioException("Coordenadas inválidas");
        }
        g.setLatitud(r.getLatitud());
        g.setLongitud(r.getLongitud());
        g.setVelocidad(r.getVelocidad());
        g.setReferencia(r.getReferencia());
        g.setRegistradoPor(UsuarioActual.username().orElse("dispositivo"));
        return ubicacionRepository.save(g);
    }

    public List<UbicacionGps> ultimasPosiciones() {
        return ubicacionRepository.ultimasPosiciones();
    }

    public List<UbicacionGps> recorrido(Long maquinariaId) {
        return ubicacionRepository.findByMaquinariaIdOrderByFechaHoraAsc(maquinariaId);
    }
}
