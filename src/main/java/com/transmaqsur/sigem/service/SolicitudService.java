package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Solicitud;
import com.transmaqsur.sigem.model.enums.EstadoSolicitud;
import com.transmaqsur.sigem.model.enums.Modulo;
import com.transmaqsur.sigem.model.enums.TipoNotificacion;
import com.transmaqsur.sigem.repository.SolicitudRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class SolicitudService {

    private final SolicitudRepository solicitudRepository;
    private final NotificacionService notificacionService;

    @Transactional(readOnly = true)
    public List<Solicitud> listar() {
        return solicitudRepository.findAllByOrderByIdDesc();
    }

    /** Solicitudes que aún pueden cotizarse. */
    @Transactional(readOnly = true)
    public List<Solicitud> listarPendientes() {
        return solicitudRepository.findByEstadoInOrderByIdDesc(List.of(EstadoSolicitud.REGISTRADA, EstadoSolicitud.COTIZADA));
    }

    @Transactional(readOnly = true)
    public Solicitud obtener(Long id) {
        return solicitudRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Solicitud", id));
    }

    public Solicitud guardar(Solicitud form) {
        if (form.getFechaFin().isBefore(form.getFechaInicio())) {
            throw new NegocioException("La fecha de fin no puede ser anterior a la fecha de inicio");
        }
        if (form.getContacto() != null && !form.getContacto().getCliente().getId().equals(form.getCliente().getId())) {
            throw new NegocioException("El contacto seleccionado no pertenece al cliente");
        }
        if (form.isNuevo()) {
            form.setEstado(EstadoSolicitud.REGISTRADA);
            Solicitud s = solicitudRepository.save(form);
            s.setCodigo(Entidades.codigo("SOL", s.getId()));
            notificacionService.notificarPermiso(Modulo.SOLICITUDES.getPermisoGestionar(), "Nueva solicitud " + s.getCodigo(),
                    s.getCliente().getRazonSocial() + " solicita " + s.getTipoServicio().getLabel().toLowerCase()
                            + " del " + s.getFechaInicio() + " al " + s.getFechaFin() + ".",
                    TipoNotificacion.INFO, "/solicitudes/" + s.getId(), null);
            return s;
        }
        Solicitud s = obtener(form.getId());
        if (s.getEstado() != EstadoSolicitud.REGISTRADA && s.getEstado() != EstadoSolicitud.COTIZADA) {
            throw new NegocioException("La solicitud " + s.getCodigo() + " ya no se puede editar (" + s.getEstado().getLabel() + ")");
        }
        Entidades.copiar(form, s, "codigo", "estado");
        return s;
    }

    public void cambiarEstado(Long id, EstadoSolicitud estado) {
        Solicitud s = obtener(id);
        if (estado == EstadoSolicitud.RECHAZADA || estado == EstadoSolicitud.ANULADA) {
            if (s.getEstado() == EstadoSolicitud.ATENDIDA || s.getEstado() == EstadoSolicitud.APROBADA) {
                throw new NegocioException("No se puede " + (estado == EstadoSolicitud.ANULADA ? "anular" : "rechazar")
                        + " una solicitud " + s.getEstado().getLabel().toLowerCase());
            }
        }
        s.setEstado(estado);
    }
}
