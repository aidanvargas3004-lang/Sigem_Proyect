package com.transur.sigem.rrhh.service;

import com.transur.sigem.rrhh.dto.DocumentoRequest;
import com.transur.sigem.rrhh.dto.DocumentoResponse;
import com.transur.sigem.rrhh.entity.DocumentoEmpleado;
import com.transur.sigem.rrhh.entity.Empleado;
import com.transur.sigem.rrhh.repository.DocumentoEmpleadoRepository;
import com.transur.sigem.rrhh.repository.EmpleadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DocumentoService {

    private final DocumentoEmpleadoRepository docRepository;
    private final EmpleadoRepository empleadoRepository;

    @Transactional
    public DocumentoResponse crear(DocumentoRequest request) {
        // Validar que fechas sean lógicas
        if (request.getFechaVencimiento().isBefore(request.getFechaEmision())) {
            throw new RuntimeException("La fecha de vencimiento no puede ser anterior a la emisión");
        }

        Empleado empleado = empleadoRepository.findById(request.getEmpleadoId())
                .orElseThrow(() -> new RuntimeException("Empleado no encontrado"));

        DocumentoEmpleado doc = new DocumentoEmpleado();
        doc.setEmpleado(empleado);
        doc.setTipoDocumento(request.getTipoDocumento());
        doc.setNumeroDocumento(request.getNumeroDocumento());
        doc.setFechaEmision(request.getFechaEmision());
        doc.setFechaVencimiento(request.getFechaVencimiento());
        doc.setRutaArchivo(request.getRutaArchivo());
        doc.setEstado(true);

        return mapToResponse(docRepository.save(doc));
    }

    public List<DocumentoResponse> listarPorEmpleado(Long empleadoId) {
        return docRepository.findByEmpleadoId(empleadoId).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // Endpoint útil para dashboard: documentos próximos a vencer (30 días)
    public List<DocumentoResponse> listarProximosAVencer() {
        LocalDate hoy = LocalDate.now();
        LocalDate en30Dias = hoy.plusDays(30);

        return docRepository.findByFechaVencimientoBetweenAndEstadoTrue(hoy, en30Dias).stream()
                .map(d -> {
                    DocumentoResponse resp = mapToResponse(d);
                    resp.setProximoAVencer(true);
                    return resp;
                })
                .collect(Collectors.toList());
    }

    // Documentos ya vencidos
    public List<DocumentoResponse> listarVencidos() {
        return docRepository.findByFechaVencimientoBeforeAndEstadoTrue(LocalDate.now()).stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private DocumentoResponse mapToResponse(DocumentoEmpleado d) {
        return DocumentoResponse.builder()
                .id(d.getId())
                .empleadoNombre(d.getEmpleado().getNombres() + " " + d.getEmpleado().getApellidos())
                .tipoDocumento(d.getTipoDocumento())
                .numeroDocumento(d.getNumeroDocumento())
                .fechaEmision(d.getFechaEmision())
                .fechaVencimiento(d.getFechaVencimiento())
                .rutaArchivo(d.getRutaArchivo())
                .estado(d.getEstado())
                .proximoAVencer(false)
                .build();
    }
}