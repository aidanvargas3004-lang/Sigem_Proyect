package com.transur.sigem.comercial.service;

import com.transur.sigem.comercial.dto.AlquilerRequest; // Crea este DTO
import com.transur.sigem.comercial.entity.Alquiler;
import com.transur.sigem.comercial.entity.AlquilerDetalle;
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.repository.AlquilerRepository;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.entity.Tarifa;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.maquinaria.repository.TarifaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlquilerService {
    private final AlquilerRepository alquilerRepository;
    private final ContratoRepository contratoRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final TarifaRepository tarifaRepository;

    @Transactional
    public Alquiler crear(AlquilerRequest request) {
        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        if (!"ALQUILER".equals(contrato.getTipoContrato())) {
            throw new RuntimeException("El contrato no es de tipo ALQUILER");
        }

        Alquiler a = new Alquiler();
        a.setContrato(contrato);
        a.setFechaInicioReal(request.getFechaInicioReal() != null ? request.getFechaInicioReal() : LocalDateTime.now());
        a.setEstado("EN_CURSO");

        List<AlquilerDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var dto : request.getDetalles()) {
            Maquinaria maq = maquinariaRepository.findById(dto.getMaquinariaId())
                    .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

            Tarifa tarifa = tarifaRepository.findById(dto.getTarifaId())
                    .orElseThrow(() -> new RuntimeException("Tarifa no encontrada"));

            AlquilerDetalle det = new AlquilerDetalle();
            det.setAlquiler(a);
            det.setMaquinaria(maq);
            det.setTarifa(tarifa);
            det.setUnidadesTiempo(dto.getUnidadesTiempo());
            det.setPrecioUnitario(tarifa.getPrecio()); // Snapshot del precio
            det.setSubtotal(tarifa.getPrecio().multiply(BigDecimal.valueOf(dto.getUnidadesTiempo())));
            if (!"OPERATIVO".equals(maq.getEstadoOperativo())) {
                throw new RuntimeException("La maquinaria " + maq.getCodigoInterno() + " no está disponible para alquilar");
            }
            maq.setEstadoOperativo("ALQUILADA");
            maquinariaRepository.save(maq);

            total = total.add(det.getSubtotal());
            detalles.add(det);
        }

        a.setDetalles(detalles);
        return alquilerRepository.save(a);
    }

    public List<Alquiler> listar() { return alquilerRepository.findAll(); }

    @Transactional
    public Alquiler finalizar(Long id) {
        Alquiler alquiler = alquilerRepository.findById(id).orElseThrow(() -> new RuntimeException("Alquiler no encontrado"));
        if ("FINALIZADO".equals(alquiler.getEstado())) return alquiler;
        alquiler.setFechaFinReal(LocalDateTime.now());
        alquiler.setEstado("FINALIZADO");
        if (alquiler.getDetalles() != null) {
            alquiler.getDetalles().forEach(d -> {
                if (d.getMaquinaria() != null) {
                    d.getMaquinaria().setEstadoOperativo("OPERATIVO");
                    maquinariaRepository.save(d.getMaquinaria());
                }
            });
        }
        return alquilerRepository.save(alquiler);
    }
}