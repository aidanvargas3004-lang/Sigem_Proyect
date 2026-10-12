package com.transur.sigem.comercial.service;

import com.transur.sigem.cliente.entity.Cliente;
import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.comercial.dto.SolicitudRequest; // Asegúrate de tener este DTO
import com.transur.sigem.comercial.entity.Solicitud;
import com.transur.sigem.comercial.repository.SolicitudRepository;
import com.transur.sigem.comercial.repository.CotizacionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SolicitudService {
    private final SolicitudRepository solicitudRepository;
    private final ClienteRepository clienteRepository;
    private final CotizacionRepository cotizacionRepository;

    @Transactional
    public Solicitud crear(SolicitudRequest request) {
        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        Solicitud s = new Solicitud();
        s.setCliente(cliente);
        s.setTipoSolicitud(request.getTipoSolicitud()); // VENTA, ALQUILER, SERVICIO
        s.setDescripcion(request.getDescripcion());
        s.setEstado("PENDIENTE");
        s.setFechaSolicitud(LocalDateTime.now());

        return solicitudRepository.save(s);
    }

    public List<Solicitud> listar() { return solicitudRepository.findAll(); }

    public Solicitud obtenerPorId(Long id) {
        return solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));
    }

    @Transactional
    public Solicitud actualizar(Long id, SolicitudRequest request) {
        Solicitud s = solicitudRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Solicitud no encontrada"));

        if (!"PENDIENTE".equals(s.getEstado()) || cotizacionRepository.existsBySolicitudId(id)) {
            throw new IllegalArgumentException("La solicitud ya entró al flujo comercial y no puede modificarse desde aquí");
        }

        Cliente cliente = clienteRepository.findById(request.getClienteId())
                .orElseThrow(() -> new RuntimeException("Cliente no encontrado"));

        s.setCliente(cliente);
        s.setTipoSolicitud(request.getTipoSolicitud());
        s.setDescripcion(request.getDescripcion());
        // El estado NO se cambia desde aquí, eso es parte del flujo comercial

        return solicitudRepository.save(s);
    }
}