package com.transur.sigem.marketing.service;

import com.transur.sigem.marketing.dto.LeadRequest;
import com.transur.sigem.marketing.entity.Campania;
import com.transur.sigem.marketing.entity.Lead;
import com.transur.sigem.marketing.repository.CampaniaRepository;
import com.transur.sigem.marketing.repository.LeadRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadService {

    private final LeadRepository leadRepository;
    private final CampaniaRepository campaniaRepository;
    private final MaquinariaRepository maquinariaRepository;

    @Transactional
    public Lead crear(LeadRequest request) {
        Lead l = new Lead();

        if (request.getCampaniaId() != null) {
            Campania camp = campaniaRepository.findById(request.getCampaniaId())
                    .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
            l.setCampania(camp);
        }
        if (request.getServicioId() != null) {
            Maquinaria maquinaria = maquinariaRepository.findById(request.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Maquinaria de interés no encontrada"));
            l.setServicio(maquinaria);
        }

        l.setNombres(request.getNombres());
        l.setEmpresa(request.getEmpresa());
        l.setTelefono(request.getTelefono());
        l.setEmail(request.getEmail());
        l.setOrigen(request.getOrigen());
        l.setEstado(request.getEstado() != null ? request.getEstado() : "NUEVO");
        l.setFechaRegistro(LocalDateTime.now());

        return leadRepository.save(l);
    }

    public List<Lead> listar() { return leadRepository.findAll(); }

    public List<Lead> listarPorCampania(Long campaniaId) {
        return leadRepository.findByCampaniaId(campaniaId);
    }

    @Transactional
    public Lead actualizar(Long id, LeadRequest request) {
        Lead l = leadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lead no encontrado con ID: " + id));

        // Actualizar campaña solo si cambió el ID
        if (request.getCampaniaId() != null) {
            Campania camp = campaniaRepository.findById(request.getCampaniaId())
                    .orElseThrow(() -> new RuntimeException("Campaña no encontrada"));
            l.setCampania(camp);
        } else {
            l.setCampania(null);
        }
        if (request.getServicioId() != null) {
            Maquinaria maquinaria = maquinariaRepository.findById(request.getServicioId())
                    .orElseThrow(() -> new RuntimeException("Maquinaria de interés no encontrada"));
            l.setServicio(maquinaria);
        } else {
            l.setServicio(null);
        }

        l.setNombres(request.getNombres());
        l.setEmpresa(request.getEmpresa());
        l.setTelefono(request.getTelefono());
        l.setEmail(request.getEmail());
        l.setOrigen(request.getOrigen());
        l.setEstado(request.getEstado() != null ? request.getEstado() : l.getEstado());

        return leadRepository.save(l);
    }

    @Transactional
    public void eliminar(Long id) {
        if (!leadRepository.existsById(id)) {
            throw new RuntimeException("Lead no encontrado con ID: " + id);
        }
        leadRepository.deleteById(id);
    }
    public long contarTotales() { return leadRepository.count(); }
    public List<Lead> listarUltimos(int limit) {
        return leadRepository.findAll(Sort.by(Sort.Direction.DESC, "fechaRegistro")).stream().limit(limit).toList();
    }

    // Agrega esto en LeadService.java
    public long contarPorEstado(String estado) {
        return leadRepository.countByEstado(estado);
    }

    public Lead obtenerPorId(Long id) {
        return leadRepository.findById(id).orElse(null);
    }

    public List<Lead> buscarLeads(String keyword) {
        // Busca por empresa, nombre de contacto o teléfono que contengan el texto
        return leadRepository.findByEmpresaContainingIgnoreCaseOrNombresContainingIgnoreCaseOrTelefonoContaining(
                keyword, keyword, keyword
        );
    }
}