package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Campana;
import com.transmaqsur.sigem.model.Oportunidad;
import com.transmaqsur.sigem.model.Solicitud;
import com.transmaqsur.sigem.model.enums.EstadoCampana;
import com.transmaqsur.sigem.model.enums.EtapaOportunidad;
import com.transmaqsur.sigem.repository.CampanaRepository;
import com.transmaqsur.sigem.repository.OportunidadRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/** Campañas de marketing y embudo de oportunidades comerciales. */
@Service
@RequiredArgsConstructor
@Transactional
public class MarketingService {

    private static final Map<EtapaOportunidad, Integer> PROBABILIDAD = Map.of(
            EtapaOportunidad.PROSPECTO, 10, EtapaOportunidad.CALIFICADA, 30, EtapaOportunidad.PROPUESTA, 50,
            EtapaOportunidad.NEGOCIACION, 75, EtapaOportunidad.GANADA, 100, EtapaOportunidad.PERDIDA, 0);

    private final CampanaRepository campanaRepository;
    private final OportunidadRepository oportunidadRepository;
    private final SolicitudService solicitudService;

    // ------------------------------------------------------------ Campañas

    @Transactional(readOnly = true)
    public List<Campana> listarCampanas() {
        return campanaRepository.findAllByOrderByFechaInicioDesc();
    }

    @Transactional(readOnly = true)
    public List<Campana> listarCampanasVigentes() {
        return campanaRepository.findByEstadoInOrderByNombreAsc(List.of(EstadoCampana.PLANIFICADA, EstadoCampana.ACTIVA));
    }

    @Transactional(readOnly = true)
    public Campana obtenerCampana(Long id) {
        return campanaRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Campaña", id));
    }

    public Campana guardarCampana(Campana form) {
        if (form.getFechaFin().isBefore(form.getFechaInicio())) {
            throw new NegocioException("La fecha de fin de la campaña no puede ser anterior a la de inicio");
        }
        if (form.isNuevo()) {
            return campanaRepository.save(form);
        }
        Campana c = obtenerCampana(form.getId());
        Entidades.copiar(form, c);
        return c;
    }

    @Transactional(readOnly = true)
    public List<Oportunidad> oportunidadesDeCampana(Long campanaId) {
        return oportunidadRepository.findByCampanaIdOrderByFechaCreacionDesc(campanaId);
    }

    // ------------------------------------------------------------ Oportunidades

    @Transactional(readOnly = true)
    public List<Oportunidad> listarOportunidades() {
        return oportunidadRepository.findAllByOrderByFechaCreacionDesc();
    }

    @Transactional(readOnly = true)
    public Oportunidad obtenerOportunidad(Long id) {
        return oportunidadRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Oportunidad", id));
    }

    public Oportunidad guardarOportunidad(Oportunidad form) {
        if (form.isNuevo()) {
            form.setProbabilidad(PROBABILIDAD.get(form.getEtapa()));
            return oportunidadRepository.save(form);
        }
        Oportunidad o = obtenerOportunidad(form.getId());
        Entidades.copiar(form, o, "solicitud");
        return o;
    }

    /** Avanza la oportunidad en el embudo y ajusta la probabilidad sugerida. */
    public void cambiarEtapa(Long id, EtapaOportunidad etapa) {
        Oportunidad o = obtenerOportunidad(id);
        if (o.getEtapa() == EtapaOportunidad.GANADA || o.getEtapa() == EtapaOportunidad.PERDIDA) {
            throw new NegocioException("La oportunidad ya está cerrada como " + o.getEtapa().getLabel().toLowerCase());
        }
        o.setEtapa(etapa);
        o.setProbabilidad(PROBABILIDAD.get(etapa));
        if (etapa == EtapaOportunidad.GANADA || etapa == EtapaOportunidad.PERDIDA) {
            o.setFechaCierreEstimada(LocalDate.now());
        }
    }

    /** Convierte la oportunidad en una solicitud formal para cotizar. */
    public Solicitud generarSolicitud(Long id) {
        Oportunidad o = obtenerOportunidad(id);
        if (o.getSolicitud() != null) {
            throw new NegocioException("La oportunidad ya generó la solicitud " + o.getSolicitud().getCodigo());
        }
        if (o.getEtapa() == EtapaOportunidad.PERDIDA) {
            throw new NegocioException("No se puede generar una solicitud de una oportunidad perdida");
        }
        Solicitud s = new Solicitud();
        s.setCliente(o.getCliente());
        s.setTipoServicio(o.getTipoServicio());
        s.setFechaInicio(LocalDate.now().plusDays(7));
        s.setFechaFin(LocalDate.now().plusDays(37));
        s.setDescripcion("Generada desde la oportunidad \"" + o.getTitulo() + "\""
                + (o.getDescripcion() != null ? ". " + o.getDescripcion() : ""));
        s = solicitudService.guardar(s);
        o.setSolicitud(s);
        if (o.getEtapa() == EtapaOportunidad.PROSPECTO || o.getEtapa() == EtapaOportunidad.CALIFICADA) {
            o.setEtapa(EtapaOportunidad.PROPUESTA);
            o.setProbabilidad(PROBABILIDAD.get(EtapaOportunidad.PROPUESTA));
        }
        return s;
    }
}
