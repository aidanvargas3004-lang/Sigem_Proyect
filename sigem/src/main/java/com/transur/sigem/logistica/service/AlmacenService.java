package com.transur.sigem.logistica.service;

import com.transur.sigem.logistica.dto.AlmacenRequest;
import com.transur.sigem.logistica.dto.UbicacionRequest;
import com.transur.sigem.logistica.entity.Almacen;
import com.transur.sigem.logistica.entity.UbicacionAlmacen;
import com.transur.sigem.logistica.repository.AlmacenRepository;
import com.transur.sigem.logistica.repository.UbicacionAlmacenRepository;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlmacenService {

    private final AlmacenRepository almacenRepository;
    private final UbicacionAlmacenRepository ubicacionRepository;
    private final EmpleadoRepository empleadoRepository;

    @Transactional
    public Almacen crear(AlmacenRequest request) {
        if (almacenRepository.existsByNombre(request.getNombre())) {
            throw new RuntimeException("Ya existe un almacén con ese nombre");
        }

        Almacen almacen = new Almacen();
        almacen.setNombre(request.getNombre());
        almacen.setDescripcion(request.getDescripcion());
        almacen.setDireccion(request.getDireccion());
        almacen.setEstado(true);

        if (request.getResponsableId() != null) {
            Empleado responsable = empleadoRepository.findById(request.getResponsableId())
                    .orElseThrow(() -> new RuntimeException("Empleado responsable no encontrado"));
            almacen.setResponsable(responsable);
        }

        return almacenRepository.save(almacen);
    }

    @Transactional
    public UbicacionAlmacen crearUbicacion(UbicacionRequest request) {
        Almacen almacen = almacenRepository.findById(request.getAlmacenId())
                .orElseThrow(() -> new RuntimeException("Almacén no encontrado"));

        // Validar que el código sea único dentro de ese almacén
        if (ubicacionRepository.findByCodigoAndAlmacenId(request.getCodigo(), request.getAlmacenId()).isPresent()) {
            throw new RuntimeException("Ya existe una ubicación con ese código en este almacén");
        }

        UbicacionAlmacen ubicacion = new UbicacionAlmacen();
        ubicacion.setAlmacen(almacen);
        ubicacion.setCodigo(request.getCodigo());
        ubicacion.setNombre(request.getNombre());
        ubicacion.setDescripcion(request.getDescripcion());
        ubicacion.setEstado(true);

        return ubicacionRepository.save(ubicacion);
    }

    public List<Almacen> listar() {
        List<Almacen> almacenes = almacenRepository.findAll();
        almacenes.forEach(a -> {
            if (a.getResponsable() != null) a.getResponsable().getNombres();
            if (a.getUbicaciones() != null) a.getUbicaciones().size();
        });
        return almacenes;
    }

    public List<UbicacionAlmacen> listarUbicaciones(Long almacenId) {
        return ubicacionRepository.findByAlmacenId(almacenId);
    }
}