package com.transur.sigem.marketing.service;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.marketing.dto.OportunidadRequest;
import com.transur.sigem.marketing.dto.OportunidadResponse;
import com.transur.sigem.marketing.entity.Lead;
import com.transur.sigem.marketing.entity.Oportunidad;
import com.transur.sigem.marketing.repository.LeadRepository;
import com.transur.sigem.marketing.repository.OportunidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OportunidadService {

    private final OportunidadRepository oportunidadRepository;
    private final LeadRepository leadRepository;
    private final ClienteRepository clienteRepository;

    @Transactional
    public OportunidadResponse crear(OportunidadRequest request) {
        Oportunidad o = new Oportunidad();

        if (request.getLeadId() != null) {
            Lead lead = leadRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new RuntimeException("Lead no encontrado"));
            o.setLead(lead);
        }

        if (request.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            o.setCliente(cliente);
        }

        o.setDescripcion(request.getDescripcion());
        o.setMontoEstimado(request.getMontoEstimado());
        o.setProbabilidad(request.getProbabilidad());
        o.setEstado(request.getEstado() != null ? request.getEstado() : "PROSPECTO");
        o.setFechaCreacion(LocalDateTime.now());

        return mapToResponse(oportunidadRepository.save(o));
    }

    public List<OportunidadResponse> listar() {
        return oportunidadRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private OportunidadResponse mapToResponse(Oportunidad o) {
        return OportunidadResponse.builder()
                .id(o.getId())
                .descripcion(o.getDescripcion())
                .estado(o.getEstado())
                .montoEstimado(o.getMontoEstimado())
                .probabilidad(o.getProbabilidad())

                // ✅ MAPEO CORRECTO DE FECHA Y CLIENTE
                .fechaCreacion(o.getFechaCreacion())
                .clienteNombre(o.getCliente() != null ? o.getCliente().getRazonSocial() : "Cliente Directo")
                .build();
    }

    @Transactional
    public OportunidadResponse actualizar(Long id, OportunidadRequest request) {
        Oportunidad o = oportunidadRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Oportunidad no encontrada con ID: " + id));

        // Actualizar Lead si cambió
        if (request.getLeadId() != null) {
            Lead lead = leadRepository.findById(request.getLeadId())
                    .orElseThrow(() -> new RuntimeException("Lead no encontrado"));
            o.setLead(lead);
        } else {
            o.setLead(null);
        }

        // Actualizar Cliente si cambió
        if (request.getClienteId() != null) {
            Cliente cliente = clienteRepository.findById(request.getClienteId())
                    .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));
            o.setCliente(cliente);
        } else {
            o.setCliente(null);
        }

        o.setDescripcion(request.getDescripcion());
        o.setMontoEstimado(request.getMontoEstimado());
        o.setProbabilidad(request.getProbabilidad());
        o.setEstado(request.getEstado() != null ? request.getEstado() : o.getEstado());

        return mapToResponse(oportunidadRepository.save(o));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!oportunidadRepository.existsById(id)) {
            throw new RuntimeException("Oportunidad no encontrada con ID: " + id);
        }
        oportunidadRepository.deleteById(id);
    }

    public long contarActivas() { return oportunidadRepository.countByEstado("ACTIVA"); }
    public BigDecimal sumarValoresActivos() {
        // Suma todos los montos estimados de todas las oportunidades
        BigDecimal suma = oportunidadRepository.sumAllMontoEstimado();
        return suma != null ? suma : BigDecimal.ZERO;
    }

    // Agrega esto en OportunidadService.java
    public double calcularProbabilidadPromedio() {
        List<Oportunidad> todas = oportunidadRepository.findAll();
        if (todas.isEmpty()) return 0.0;

        double suma = todas.stream()
                .mapToDouble(o -> o.getProbabilidad() != null ? o.getProbabilidad() : 0)
                .sum();

        return Math.round((suma / todas.size()) * 10.0) / 10.0;
    }

    public Oportunidad obtenerPorId(Long id) {
        return oportunidadRepository.findById(id).orElse(null);
    }

    // Agrega esto dentro de tu clase OportunidadService
    public long contarTodas() {
        return oportunidadRepository.count();
    }
}