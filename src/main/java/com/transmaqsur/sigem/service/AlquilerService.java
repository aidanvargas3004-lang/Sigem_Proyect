package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.model.Alquiler;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoOperacion;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.OrigenAsignacion;
import com.transmaqsur.sigem.repository.AlquilerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Alquiler de maquinaria y unidades por hora o por día. */
@Service
@Transactional
public class AlquilerService extends OperacionService<Alquiler> {

    private final AlquilerRepository alquilerRepository;

    public AlquilerService(AlquilerRepository repository, AsignacionService asignacionService, UnidadService unidadService,
                           NotificacionService notificacionService, AlertaService alertaService) {
        super(repository, asignacionService, unidadService, notificacionService, alertaService);
        this.alquilerRepository = repository;
    }

    @Override
    protected String prefijo() {
        return "ALQ";
    }

    @Override
    protected OrigenAsignacion origen() {
        return OrigenAsignacion.ALQUILER;
    }

    @Override
    protected String nombre() {
        return "Alquiler";
    }

    @Override
    protected String ruta() {
        return "/alquileres";
    }

    @Override
    protected void validarEspecifico(Alquiler alquiler, Unidad unidad) {
        if (alquiler.getModalidad() != Modalidad.POR_HORA && alquiler.getModalidad() != Modalidad.POR_DIA) {
            throw new NegocioException("Los alquileres se cobran por hora o por día");
        }
    }

    @Transactional(readOnly = true)
    public List<Alquiler> listar() {
        return alquilerRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Alquiler> deContrato(Long contratoId) {
        return alquilerRepository.findByContratoIdOrderByIdDesc(contratoId);
    }

    @Transactional(readOnly = true)
    public List<Alquiler> pendientesDeFacturar(Long clienteId) {
        return alquilerRepository.findByClienteIdAndEstadoAndFacturadoFalseOrderByIdAsc(clienteId, EstadoOperacion.FINALIZADO);
    }
}
