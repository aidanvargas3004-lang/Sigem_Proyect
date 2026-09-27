package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.ServicioTransporte;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.OrigenAsignacion;
import com.transmaqsur.sigem.repository.ServicioTransporteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Servicios de transporte de carga pesada. */
@Service
@Transactional
public class ServicioTransporteService extends OperacionService<ServicioTransporte> {

    private final ServicioTransporteRepository servicioRepository;

    public ServicioTransporteService(ServicioTransporteRepository repository, AsignacionService asignacionService,
                                     UnidadService unidadService, NotificacionService notificacionService, AlertaService alertaService) {
        super(repository, asignacionService, unidadService, notificacionService, alertaService);
        this.servicioRepository = repository;
    }

    @Override
    protected String prefijo() {
        return "SRV";
    }

    @Override
    protected OrigenAsignacion origen() {
        return OrigenAsignacion.SERVICIO;
    }

    @Override
    protected String nombre() {
        return "Servicio de transporte";
    }

    @Override
    protected String ruta() {
        return "/servicios";
    }

    @Override
    protected void validarEspecifico(ServicioTransporte servicio, Unidad unidad) {
        if (!unidad.getTipo().isVehiculo()) {
            throw new NegocioException("Los servicios de transporte requieren un vehículo (camión, volquete o cisterna)");
        }
        if (servicio.getModalidad() == Modalidad.POR_DIA) {
            throw new NegocioException("Los servicios de transporte se cobran por km, por viaje o por hora");
        }
        if (servicio.getAsignacion().getEmpleado() == null) {
            throw new NegocioException("Asigne el conductor del servicio");
        }
    }

    @Transactional(readOnly = true)
    public List<ServicioTransporte> listar() {
        return servicioRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<ServicioTransporte> deContrato(Long contratoId) {
        return servicioRepository.findByContratoIdOrderByIdDesc(contratoId);
    }

    @Transactional(readOnly = true)
    public List<ServicioTransporte> pendientesDeFacturar(Long clienteId) {
        return servicioRepository.findByClienteIdAndEstadoAndFacturadoFalseOrderByIdAsc(clienteId, EstadoOperacion.FINALIZADO);
    }
}
