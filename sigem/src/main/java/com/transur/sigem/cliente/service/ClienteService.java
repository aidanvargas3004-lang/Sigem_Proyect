package com.transur.sigem.cliente.service;

import com.transur.sigem.cliente.dto.ClienteRequest;
import com.transur.sigem.cliente.dto.ClienteResponse;
import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.security.entity.Rol;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.RolRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    @Transactional
    public ClienteResponse crear(ClienteRequest request) {
        if (clienteRepository.existsByDocumento(request.getDocumento())) {
            throw new RuntimeException("Ya existe un cliente con ese documento: " + request.getDocumento());
        }

        Cliente cliente = new Cliente();
        cliente.setRazonSocial(request.getRazonSocial());
        cliente.setDocumento(request.getDocumento());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setTipoCliente(request.getTipoCliente());
        cliente.setEstado(true);

        Cliente guardado = clienteRepository.save(cliente);
        return mapToResponse(guardado);
    }

    public List<ClienteResponse> listarTodos() {
        return clienteRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ClienteResponse obtenerPorId(Long id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado con ID: " + id));
        return mapToResponse(cliente);
    }

    @Transactional
    public ClienteResponse actualizar(Long id, ClienteRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        // Validar unicidad de documento solo si cambió
        if (!cliente.getDocumento().equals(request.getDocumento()) &&
                clienteRepository.existsByDocumento(request.getDocumento())) {
            throw new RuntimeException("Documento ya registrado en otro cliente");
        }

        cliente.setRazonSocial(request.getRazonSocial());
        cliente.setDocumento(request.getDocumento());
        cliente.setEmail(request.getEmail());
        cliente.setTelefono(request.getTelefono());
        cliente.setDireccion(request.getDireccion());
        cliente.setTipoCliente(request.getTipoCliente());

        return mapToResponse(clienteRepository.save(cliente));
    }

    @Transactional
    public void eliminar(Long id) {
        if (!clienteRepository.existsById(id)) {
            throw new RuntimeException("Cliente no encontrado");
        }
        // Soft delete recomendado para clientes (mejor cambiar estado a false)
        Cliente cliente = clienteRepository.findById(id).get();
        cliente.setEstado(false);
        clienteRepository.save(cliente);
    }

    private ClienteResponse mapToResponse(Cliente c) {
        return ClienteResponse.builder()
                .id(c.getId())
                .razonSocial(c.getRazonSocial())
                .documento(c.getDocumento())
                .email(c.getEmail())
                .telefono(c.getTelefono())
                .direccion(c.getDireccion())
                .tipoCliente(c.getTipoCliente())
                .estado(c.getEstado())
                .build();
    }

    public List<Cliente> listarDirectorio() {
        // Retorna todos los clientes activos ordenados por nombre
        return clienteRepository.findAllByOrderByRazonSocialAsc();
    }

    @Transactional
    public void crearClienteConUsuario(ClienteRequest req) {
        // Validación de duplicados
        if (clienteRepository.existsByDocumento(req.getDocumento())) {
            throw new RuntimeException("Ya existe un cliente con ese documento: " + req.getDocumento());
        }

        // 1. Crear Cliente
        Cliente cliente = new Cliente();
        cliente.setDocumento(req.getDocumento());
        cliente.setRazonSocial(req.getRazonSocial());
        cliente.setTipoCliente(req.getTipoCliente());
        cliente.setDireccion(req.getDireccion());
        cliente.setTelefono(req.getTelefono());
        cliente.setEmail(req.getEmail());
        cliente.setFechaRegistro(LocalDateTime.now());
        cliente.setEstado(true); // Asegurar estado activo

        Cliente savedCliente = clienteRepository.save(cliente);

        // 2. Buscar Rol
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException("Rol 'CLIENTE' no encontrado"));

        // 3. Crear Usuario
        Usuario usuario = new Usuario();
        usuario.setUsername(req.getUsername());
        usuario.setPassword(passwordEncoder.encode(req.getNewPassword()));
        usuario.setRol(rolCliente);

        // Usar datos del cliente para llenar campos obligatorios de Usuario
        usuario.setNombres(savedCliente.getRazonSocial());
        usuario.setApellidos("");
        usuario.setEmail(req.getEmail() != null ? req.getEmail() : req.getUsername() + "@sigem.com");
        usuario.setEstado(true);

        usuario.setCliente(savedCliente);
        usuarioRepository.save(usuario);
        savedCliente.setUsuario(usuario);
        clienteRepository.save(savedCliente);
    }


}