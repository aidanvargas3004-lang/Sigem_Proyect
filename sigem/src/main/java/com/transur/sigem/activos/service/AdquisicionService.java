package com.transur.sigem.activos.service;

import com.transur.sigem.activos.dto.AdquisicionRequest;
import com.transur.sigem.activos.dto.AdquisicionResponse;
import com.transur.sigem.activos.dto.DetalleAdquisicionDTO;
import com.transur.sigem.activos.entity.AdquisicionMaquinaria;
import com.transur.sigem.activos.entity.DetalleAdquisicion;
import com.transur.sigem.activos.repository.AdquisicionRepository;
import com.transur.sigem.compras.entity.Proveedor;
import com.transur.sigem.compras.repository.ProveedorRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdquisicionService {

    private final AdquisicionRepository adquisicionRepository;
    private final ProveedorRepository proveedorRepository;
    private final MaquinariaRepository maquinariaRepository;

    @Transactional
    public AdquisicionResponse crear(AdquisicionRequest request) {
        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        AdquisicionMaquinaria adquisicion = new AdquisicionMaquinaria();
        adquisicion.setProveedor(proveedor);
        adquisicion.setTipoAdquisicion(request.getTipoAdquisicion());
        adquisicion.setFechaAdquisicion(request.getFechaAdquisicion());
        adquisicion.setDestinoInicial(request.getDestinoInicial());
        adquisicion.setNumeroSerieFabrica(request.getNumeroSerieFabrica());
        adquisicion.setDocumentosLegalUrl(request.getDocumentosLegalUrl());
        adquisicion.setEstado("ACTIVA");

        List<DetalleAdquisicion> detallesList = new ArrayList<>();
        BigDecimal costoTotalCalculado = BigDecimal.ZERO;

        for (DetalleAdquisicionDTO dto : request.getDetalles()) {
            Maquinaria maq = maquinariaRepository.findById(dto.getMaquinariaId())
                    .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada: " + dto.getMaquinariaId()));

            DetalleAdquisicion detalle = new DetalleAdquisicion();
            detalle.setAdquisicion(adquisicion);
            detalle.setMaquinaria(maq);
            detalle.setPrecioUnitario(dto.getPrecioUnitario());
            detalle.setObservaciones(dto.getObservaciones());

            costoTotalCalculado = costoTotalCalculado.add(dto.getPrecioUnitario());
            detallesList.add(detalle);
        }

        // Validar que el costo total coincida con la suma de detalles (opcional pero recomendado)
        // O usar el costoTotal enviado si incluye gastos adicionales no reflejados en detalles
        adquisicion.setCostoTotal(request.getDetalles().size() > 0 ?
                costoTotalCalculado : BigDecimal.ZERO);
        adquisicion.setDetalles(detallesList);

        return mapToResponse(adquisicionRepository.save(adquisicion));
    }

    public List<AdquisicionResponse> listar() {
        return adquisicionRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private AdquisicionResponse mapToResponse(AdquisicionMaquinaria a) {
        return AdquisicionResponse.builder()
                .id(a.getId())
                .proveedorNombre(a.getProveedor().getRazonSocial()) // Asegúrate que Proveedor tenga este campo
                .tipoAdquisicion(a.getTipoAdquisicion())
                .costoTotal(a.getCostoTotal())
                .fechaAdquisicion(a.getFechaAdquisicion())
                .destinoInicial(a.getDestinoInicial())
                .estado(a.getEstado())
                .cantidadUnidades(a.getDetalles() != null ? a.getDetalles().size() : 0)
                .build();
    }

    // En AdquisicionService.java

    @Transactional
    public AdquisicionResponse actualizar(Long id, AdquisicionRequest request) {
        AdquisicionMaquinaria adquisicion = adquisicionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adquisición no encontrada con ID: " + id));

        Proveedor proveedor = proveedorRepository.findById(request.getProveedorId())
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));

        // Actualizar cabecera
        adquisicion.setProveedor(proveedor);
        adquisicion.setTipoAdquisicion(request.getTipoAdquisicion());
        adquisicion.setFechaAdquisicion(request.getFechaAdquisicion());
        adquisicion.setDestinoInicial(request.getDestinoInicial());
        adquisicion.setNumeroSerieFabrica(request.getNumeroSerieFabrica());
        adquisicion.setDocumentosLegalUrl(request.getDocumentosLegalUrl());

        // Recalcular costo total basado en nuevos detalles
        BigDecimal nuevoCostoTotal = BigDecimal.ZERO;
        List<DetalleAdquisicion> nuevosDetalles = new ArrayList<>();

        for (DetalleAdquisicionDTO dto : request.getDetalles()) {
            Maquinaria maq = maquinariaRepository.findById(dto.getMaquinariaId())
                    .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

            DetalleAdquisicion detalle = new DetalleAdquisicion();
            detalle.setAdquisicion(adquisicion);
            detalle.setMaquinaria(maq);
            detalle.setPrecioUnitario(dto.getPrecioUnitario());
            detalle.setObservaciones(dto.getObservaciones());

            nuevoCostoTotal = nuevoCostoTotal.add(dto.getPrecioUnitario());
            nuevosDetalles.add(detalle);
        }

        // Reemplazar detalles antiguos (orphanRemoval = true en entidad se encarga de borrar los viejos)
        adquisicion.getDetalles().clear();
        adquisicion.getDetalles().addAll(nuevosDetalles);
        adquisicion.setCostoTotal(nuevoCostoTotal);

        return mapToResponse(adquisicionRepository.save(adquisicion));
    }

    @Transactional
    public void cancelar(Long id) {
        AdquisicionMaquinaria adquisicion = adquisicionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adquisición no encontrada"));

        if ("CANCELADA".equals(adquisicion.getEstado())) {
            throw new RuntimeException("La adquisición ya está cancelada");
        }

        adquisicion.setEstado("CANCELADA");
        adquisicionRepository.save(adquisicion);
    }

    public AdquisicionResponse obtenerPorId(Long id) {
        AdquisicionMaquinaria adquisicion = adquisicionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Adquisición no encontrada con ID: " + id));
        return mapToResponse(adquisicion);
    }
}