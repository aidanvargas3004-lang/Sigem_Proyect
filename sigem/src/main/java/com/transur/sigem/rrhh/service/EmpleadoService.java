package com.transur.sigem.rrhh.service;

import com.transur.sigem.rrhh.dto.EmpleadoRequest;
import com.transur.sigem.rrhh.dto.EmpleadoResponse;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmpleadoService {

    private final EmpleadoRepository empleadoRepository;
    private final UsuarioRepository usuarioRepository;

    @Transactional
    public EmpleadoResponse crear(EmpleadoRequest request) {
        if (empleadoRepository.existsByDocumento(request.getDocumento())) {
            throw new RuntimeException("Ya existe un empleado con documento: " + request.getDocumento());
        }

        Empleado emp = new Empleado();
        emp.setDocumento(request.getDocumento());
        emp.setNombres(request.getNombres());
        emp.setApellidos(request.getApellidos());
        emp.setCargo(request.getCargo());
        emp.setTelefono(request.getTelefono());
        emp.setEmail(request.getEmail());
        emp.setFechaIngreso(request.getFechaIngreso());
        emp.setEstado(true);

        // Vincular usuario si se proporciona ID
        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            emp.setUsuario(usuario);
        }

        return mapToResponse(empleadoRepository.save(emp));
    }

    @Transactional
    public EmpleadoResponse actualizar(Long id, EmpleadoRequest request) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        // Validar unicidad de documento solo si cambió
        if (!emp.getDocumento().equals(request.getDocumento()) &&
                empleadoRepository.existsByDocumento(request.getDocumento())) {
            throw new RuntimeException("Documento ya registrado en otro empleado");
        }

        emp.setDocumento(request.getDocumento());
        emp.setNombres(request.getNombres());
        emp.setApellidos(request.getApellidos());
        emp.setCargo(request.getCargo());
        emp.setTelefono(request.getTelefono());
        emp.setEmail(request.getEmail());
        emp.setFechaIngreso(request.getFechaIngreso());

        // Actualizar vínculo de usuario si cambió
        if (request.getUsuarioId() != null) {
            Usuario usuario = usuarioRepository.findById(request.getUsuarioId())
                    .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));
            emp.setUsuario(usuario);
        } else {
            emp.setUsuario(null); // Desvincular si viene null
        }

        return mapToResponse(empleadoRepository.save(emp));
    }

    @Transactional
    public void cambiarEstado(Long id, Boolean nuevoEstado) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        emp.setEstado(nuevoEstado);
        empleadoRepository.save(emp);
    }

    public EmpleadoResponse obtenerPorId(Long id) {
        Empleado emp = empleadoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));
        return mapToResponse(emp);
    }

    public List<EmpleadoResponse> listarTodos() {
        return empleadoRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<EmpleadoResponse> listarActivos() {
        return empleadoRepository.findByEstadoTrue().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private EmpleadoResponse mapToResponse(Empleado e) {
        return EmpleadoResponse.builder()
                .id(e.getId())
                .documento(e.getDocumento())
                .nombres(e.getNombres())
                .apellidos(e.getApellidos())
                .cargo(e.getCargo())
                .telefono(e.getTelefono())
                .email(e.getEmail())
                .fechaIngreso(e.getFechaIngreso())
                .estado(e.getEstado())
                .username(e.getUsuario() != null ? e.getUsuario().getUsername() : null)
                .cantidadDocumentos(e.getDocumentos() != null ? e.getDocumentos().size() : 0)
                .build();
    }
}