package com.transur.sigem.mantenimiento.service;

import com.transur.sigem.logistica.entity.Repuesto;
import com.transur.sigem.logistica.repository.RepuestoRepository;
import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.mantenimiento.dto.MantenimientoRequest;
import com.transur.sigem.mantenimiento.dto.MantenimientoResponse;
import com.transur.sigem.mantenimiento.dto.RepuestoUsadoDTO;
import com.transur.sigem.mantenimiento.entity.Mantenimiento;
import com.transur.sigem.mantenimiento.entity.MantenimientoRepuesto;
import com.transur.sigem.mantenimiento.repository.MantenimientoRepository;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
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
public class MantenimientoService {

    private final MantenimientoRepository mantenimientoRepository;
    private final MaquinariaRepository maquinariaRepository;
    private final EmpleadoRepository empleadoRepository;
    private final RepuestoRepository repuestoRepository;

    @Transactional
    public MantenimientoResponse crear(MantenimientoRequest request) {
        Maquinaria maq = maquinariaRepository.findById(request.getMaquinariaId())
                .orElseThrow(() -> new RuntimeException("Maquinaria no encontrada"));

        Mantenimiento mant = new Mantenimiento();
        mant.setMaquinaria(maq);

        if (request.getEmpleadoId() != null) {
            Empleado emp = empleadoRepository.findById(request.getEmpleadoId())
                    .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
            mant.setEmpleado(emp);
        }

        mant.setTipoMantenimiento(request.getTipoMantenimiento());
        mant.setDescripcion(request.getDescripcion());
        mant.setFechaProgramada(request.getFechaProgramada());
        mant.setFechaInicio(request.getFechaInicio());
        mant.setFechaFin(request.getFechaFin());
        mant.setEstado(request.getEstado() != null ? request.getEstado() : "PROGRAMADO");
        mant.setObservaciones(request.getObservaciones());

        List<MantenimientoRepuesto> repuestosList = new ArrayList<>();
        BigDecimal costoTotal = BigDecimal.ZERO;

        if (request.getRepuestosUsados() != null) {
            for (RepuestoUsadoDTO dto : request.getRepuestosUsados()) {
                Repuesto rep = repuestoRepository.findById(dto.getRepuestoId())
                        .orElseThrow(() -> new RuntimeException("Repuesto no encontrado"));

                MantenimientoRepuesto mr = new MantenimientoRepuesto();
                mr.setMantenimiento(mant);
                mr.setRepuesto(rep);
                mr.setCantidad(dto.getCantidad());
                mr.setCostoUnitario(dto.getCostoUnitario());

                costoTotal = costoTotal.add(dto.getCostoUnitario().multiply(BigDecimal.valueOf(dto.getCantidad())));
                repuestosList.add(mr);

                // ⚠️ NOTA: Aquí podrías llamar a un servicio de inventario para descontar stock
                // inventoryService.descontarStock(rep.getId(), dto.getCantidad());
            }
        }

        mant.setCosto(costoTotal);
        mant.setRepuestosUsados(repuestosList);

        return mapToResponse(mantenimientoRepository.save(mant));
    }

    public List<MantenimientoResponse> listar() {
        return mantenimientoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MantenimientoResponse mapToResponse(Mantenimiento m) {
        String techName = null;
        if (m.getEmpleado() != null) {
            techName = m.getEmpleado().getNombres() + " " + m.getEmpleado().getApellidos();
        }

        return MantenimientoResponse.builder()
                .id(m.getId())
                .maquinariaCodigo(m.getMaquinaria().getCodigoInterno())
                .tecnicoNombre(techName)
                .tipoMantenimiento(m.getTipoMantenimiento())
                .descripcion(m.getDescripcion())
                .fechaProgramada(m.getFechaProgramada())
                .fechaInicio(m.getFechaInicio())
                .fechaFin(m.getFechaFin())
                .costoTotal(m.getCosto())
                .estado(m.getEstado())
                .cantidadRepuestos(m.getRepuestosUsados() != null ? m.getRepuestosUsados().size() : 0)
                .build();
    }
}