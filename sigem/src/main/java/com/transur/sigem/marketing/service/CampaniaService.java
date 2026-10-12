package com.transur.sigem.marketing.service;

import com.transur.sigem.marketing.dto.CampaniaRequest;
import com.transur.sigem.marketing.entity.Campania;
import com.transur.sigem.marketing.repository.CampaniaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CampaniaService {

    private final CampaniaRepository campaniaRepository;

    @Transactional
    public Campania crear(CampaniaRequest request) {
        Campania c = new Campania();
        c.setNombre(request.getNombre());
        c.setDescripcion(request.getDescripcion());
        c.setFechaInicio(request.getFechaInicio());
        c.setFechaFin(request.getFechaFin());
        c.setPresupuesto(request.getPresupuesto());
        c.setEstado(request.getEstado() != null ? request.getEstado() : "ACTIVA");
        return campaniaRepository.save(c);
    }

    public List<Campania> listar() { return campaniaRepository.findAll(); }

    @Transactional
    public void actualizarCampania(Long id, CampaniaRequest request) {
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada con ID: " + id));

        campania.setNombre(request.getNombre());
        campania.setDescripcion(request.getDescripcion());
        campania.setFechaInicio(request.getFechaInicio());
        campania.setFechaFin(request.getFechaFin());
        campania.setPresupuesto(request.getPresupuesto());
        campania.setEstado(request.getEstado());

        campaniaRepository.save(campania);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!campaniaRepository.existsById(id)) {
            throw new RuntimeException("Campaña no encontrada con ID: " + id);
        }
        campaniaRepository.deleteById(id);
    }

    public long contarActivas() { return campaniaRepository.countByEstado("ACTIVA"); }

    @Transactional
    public void crearCampania(CampaniaRequest request) {
        Campania campania = new Campania();
        campania.setNombre(request.getNombre());
        campania.setDescripcion(request.getDescripcion());
        campania.setFechaInicio(request.getFechaInicio());
        campania.setFechaFin(request.getFechaFin());
        campania.setPresupuesto(request.getPresupuesto());
        campania.setEstado(request.getEstado());

        // Inicializar leads generados en 0 por defecto
        campania.setLeadsGenerados(0);

        campaniaRepository.save(campania);
    }

    public Campania obtenerPorId(Long id) {
        return campaniaRepository.findById(id).orElse(null);
    }

    @Transactional
    public Campania actualizar(Long id, CampaniaRequest request) {
        Campania campania = campaniaRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Campaña no encontrada con ID: " + id));

        campania.setNombre(request.getNombre());
        campania.setDescripcion(request.getDescripcion());
        campania.setFechaInicio(request.getFechaInicio());
        campania.setFechaFin(request.getFechaFin());
        campania.setPresupuesto(request.getPresupuesto());
        campania.setEstado(request.getEstado());

        return campaniaRepository.save(campania);
    }
}