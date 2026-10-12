package com.transur.sigem.comercial.service;

import com.transur.sigem.comercial.dto.VentaRequest; // Crea este DTO si no existe
import com.transur.sigem.comercial.entity.Contrato;
import com.transur.sigem.comercial.entity.Venta;
import com.transur.sigem.comercial.entity.VentaDetalle;
import com.transur.sigem.comercial.repository.ContratoRepository;
import com.transur.sigem.comercial.repository.VentaRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VentaService {
    private final VentaRepository ventaRepository;
    private final ContratoRepository contratoRepository;
    private final MaquinariaRepository maquinariaRepository;

    @Transactional
    public Venta crear(VentaRequest request) {
        if (ventaRepository.existsByNumero(request.getNumero())) {
            throw new RuntimeException("Número de venta ya existe");
        }

        Contrato contrato = contratoRepository.findById(request.getContratoId())
                .orElseThrow(() -> new RuntimeException("Contrato no encontrado"));

        // Validar que el contrato sea de tipo VENTA
        if (!"VENTA".equals(contrato.getTipoContrato())) {
            throw new RuntimeException("El contrato no es de tipo VENTA");
        }

        Venta v = new Venta();
        v.setContrato(contrato);
        v.setNumero(request.getNumero());
        v.setFechaVenta(LocalDate.now());
        v.setEstado("COMPLETADA");

        List<VentaDetalle> detalles = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (var dto : request.getDetalles()) {
            Maquinaria maq = maquinariaRepository.findById(dto.getMaquinariaId())
                    .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

            if (!"OPERATIVO".equals(maq.getEstadoOperativo())) {
                throw new RuntimeException("La maquinaria " + maq.getCodigoInterno() + " no está disponible para venta");
            }
            maq.setEstadoOperativo("VENDIDO");
            maquinariaRepository.save(maq);

            VentaDetalle det = new VentaDetalle();
            det.setVenta(v);
            det.setMaquinaria(maq);
            det.setPrecioFinal(dto.getPrecioFinal());
            det.setCantidad(dto.getCantidad());
            det.setSubtotal(dto.getPrecioFinal().multiply(BigDecimal.valueOf(dto.getCantidad())));

            total = total.add(det.getSubtotal());
            detalles.add(det);
        }

        v.setDetalles(detalles);
        v.setTotalFinal(total);

        // Opcional: Cambiar estado del contrato a FINALIZADO
        contrato.setEstado("FINALIZADO");
        contratoRepository.save(contrato);

        return ventaRepository.save(v);
    }

    public List<Venta> listar() { return ventaRepository.findAll(); }
}