package com.transur.sigem.sistema.service;

import com.transur.sigem.security.entity.Usuario;
import com.transur.sigem.security.repository.UsuarioRepository;
import com.transur.sigem.sistema.dto.ResumenAuditoriaDTO;
import com.transur.sigem.sistema.entity.Auditoria;
import com.transur.sigem.sistema.repository.AuditoriaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditoriaService {

    private final AuditoriaRepository auditoriaRepository;
    private final UsuarioRepository usuarioRepository;

    // ✅ REGISTRO DE AUDITORÍA (Para llamar desde otros servicios)
    @Transactional
    public void registrar(Long usuarioId, String modulo, String accion,
                          String entidad, Long entidadId,
                          String datosAnteriores, String datosNuevos) {
        Auditoria aud = new Auditoria();

        if (usuarioId != null) {
            Usuario u = usuarioRepository.findById(usuarioId).orElse(null);
            aud.setUsuario(u);
        }

        aud.setModulo(modulo);
        aud.setAccion(accion);
        aud.setEntidad(entidad);
        aud.setEntidadId(entidadId);
        aud.setDatosAnteriores(datosAnteriores);
        aud.setDatosNuevos(datosNuevos);
        aud.setFecha(LocalDateTime.now());

        auditoriaRepository.save(aud);
    }

    // ✅ LISTAR POR MÓDULO
    public List<ResumenAuditoriaDTO> listarPorModulo(String modulo) {
        return auditoriaRepository.findByModuloOrderByFechaDesc(modulo).stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    // ✅ LISTAR ÚLTIMAS N ACCIONES (CORREGIDO CON PAGINACIÓN Y MAPPER CORRECTO)
    public List<ResumenAuditoriaDTO> listarUltimas(int limit) {
        return auditoriaRepository.findAll(PageRequest.of(0, limit, Sort.by("fecha").descending()))
                .stream()
                .map(this::mapToDTO) // ✅ Ahora sí encuentra el método
                .collect(Collectors.toList());
    }

    // ✅ MAPPER PRIVADO UNIFICADO
    private ResumenAuditoriaDTO mapToDTO(Auditoria a) {
        return ResumenAuditoriaDTO.builder()
                .modulo(a.getModulo())
                .accion(a.getAccion())
                .entidad(a.getEntidad())
                .entidadId(a.getEntidadId())
                .nombreUsuario(a.getUsuario() != null ? a.getUsuario().getUsername() : "SISTEMA")
                .fecha(a.getFecha().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")))
                .build();
    }
}