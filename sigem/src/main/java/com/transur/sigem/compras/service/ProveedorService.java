package com.transur.sigem.compras.service;

import com.transur.sigem.compras.dto.ProveedorRequest;
import com.transur.sigem.compras.entity.Proveedor;
import com.transur.sigem.compras.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    @Transactional
    public Proveedor crear(ProveedorRequest request) {
        if (proveedorRepository.existsByDocumento(request.getDocumento())) {
            throw new RuntimeException("Ya existe un proveedor con ese documento");
        }

        Proveedor p = new Proveedor();
        p.setTipoProveedor(request.getTipoProveedor());
        p.setRazonSocial(request.getRazonSocial());
        p.setDocumento(request.getDocumento());
        p.setTelefono(request.getTelefono());
        p.setEmail(request.getEmail());
        p.setDireccion(request.getDireccion());
        p.setEstado(true);
        p.setFechaRegistro(LocalDateTime.now());

        return proveedorRepository.save(p);
    }

    public List<Proveedor> listar() {
        return proveedorRepository.findAll();
    }

    public Proveedor obtenerPorId(Long id) {
        return proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado"));
    }

    @Transactional
    public Proveedor actualizar(Long id, ProveedorRequest request) {
        Proveedor p = obtenerPorId(id);
        p.setRazonSocial(request.getRazonSocial());
        p.setTelefono(request.getTelefono());
        p.setEmail(request.getEmail());
        p.setDireccion(request.getDireccion());
        return proveedorRepository.save(p);
    }

    @Transactional
    public void eliminar(Long id) {
        Proveedor p = obtenerPorId(id);
        p.setEstado(false); // Soft delete
        proveedorRepository.save(p);
    }
}