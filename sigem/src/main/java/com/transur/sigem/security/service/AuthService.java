package com.transur.sigem.security.service;

import com.transur.sigem.security.entity.Rol;
import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.dto.AuthResponse;
import com.transur.sigem.security.dto.LoginRequest;
import com.transur.sigem.security.dto.RegisterRequest;
import com.transur.sigem.security.repository.RolRepository;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.security.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ClienteRepository clienteRepository;

    // ============================================================
    // MÉTODOS PARA API REST (Postman / Apps Móviles / Admins)
    // ============================================================

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Usuario o contraseña incorrectos"));

        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getId(), usuario.getRol().getNombre());

        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(usuario.getId())
                .username(usuario.getUsername())
                .role(usuario.getRol().getNombre())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .build();
    }

    public AuthResponse register(RegisterRequest request) {
        // El registro público nunca acepta el rol enviado por el cliente.
        // Toda cuenta creada por este endpoint es CLIENTE y además recibe su entidad de negocio.
        registrarUsuario(request);
        Usuario usuario = usuarioRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new IllegalStateException("No se pudo recuperar la cuenta creada"));
        String token = jwtUtil.generateToken(usuario.getUsername(), usuario.getId(), usuario.getRol().getNombre());
        return AuthResponse.builder()
                .token(token)
                .tokenType("Bearer")
                .userId(usuario.getId())
                .username(usuario.getUsername())
                .role(usuario.getRol().getNombre())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .build();
    }

    // ============================================================
    // ✅ MÉTODO EXCLUSIVO PARA THYMELEAF (Registro Web de Clientes)
    // ============================================================

    @Transactional
    public void registrarUsuario(RegisterRequest dto) {
        // 1. Validaciones de unicidad
        if (usuarioRepository.existsByUsername(dto.getUsername())) {
            throw new RuntimeException("El nombre de usuario '" + dto.getUsername() + "' ya está en uso.");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException("El correo '" + dto.getEmail() + "' ya está registrado.");
        }

        // 2. ASIGNAR ROL CLIENTE AUTOMÁTICAMENTE (Seguridad Crítica)
        // Nadie puede registrarse como ADMIN o GERENCIA desde la web
        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseThrow(() -> new RuntimeException(
                        "Error de configuración: El rol 'CLIENTE' no existe en la base de datos."));

        // 3. Crear y guardar usuario con rol restringido
        Usuario usuario = crearUsuarioBase(dto, rolCliente);
        usuarioRepository.save(usuario);

        // Todo registro web es un cliente real del negocio, no solo una cuenta de acceso.
        Cliente cliente = new Cliente();
        cliente.setTipoCliente(dto.getTipoCliente() == null || dto.getTipoCliente().isBlank() ? "NATURAL" : dto.getTipoCliente().toUpperCase());
        cliente.setRazonSocial((dto.getEmpresa() == null || dto.getEmpresa().isBlank())
                ? (dto.getNombres() + " " + dto.getApellidos()).trim()
                : dto.getEmpresa().trim());
        cliente.setDocumento("WEB-" + usuario.getId());
        cliente.setTelefono(dto.getTelefono());
        cliente.setEmail(dto.getEmail());
        cliente.setEstado(true);
        cliente.setFechaRegistro(java.time.LocalDateTime.now());
        cliente.setUsuario(usuario);
        clienteRepository.save(cliente);
        usuario.setCliente(cliente);
        usuarioRepository.save(usuario);
    }

    // Método privado unificado para evitar duplicación de código
    private Usuario crearUsuarioBase(RegisterRequest dto, Rol rol) {
        Usuario usuario = new Usuario();
        usuario.setUsername(dto.getUsername());
        usuario.setPassword(passwordEncoder.encode(dto.getPassword())); // Encriptación BCrypt obligatoria
        usuario.setEmail(dto.getEmail());
        usuario.setNombres(dto.getNombres());
        usuario.setApellidos(dto.getApellidos());
        usuario.setRol(rol);
        usuario.setEstado(true); // Activo por defecto al registrarse
        return usuario;
    }
}