package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Cotizacion;
import com.transmaqsur.sigem.model.CotizacionDetalle;
import com.transmaqsur.sigem.model.Solicitud;
import com.transmaqsur.sigem.model.Unidad;
import com.transmaqsur.sigem.model.enums.EstadoCotizacion;
import com.transmaqsur.sigem.model.enums.EstadoSolicitud;
import com.transmaqsur.sigem.model.enums.Modalidad;
import com.transmaqsur.sigem.model.enums.TipoServicio;
import com.transmaqsur.sigem.model.enums.TipoUnidad;
import com.transmaqsur.sigem.repository.CotizacionRepository;
import com.transmaqsur.sigem.repository.UnidadRepository;
import com.transmaqsur.sigem.util.Entidades;
import com.transmaqsur.sigem.util.Montos;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

/** Cotizaciones: borrador → enviada → aceptada / rechazada / vencida. */
@Service
@RequiredArgsConstructor
@Transactional
public class CotizacionService {

    private final CotizacionRepository cotizacionRepository;
    private final UnidadRepository unidadRepository;

    @Transactional(readOnly = true)
    public List<Cotizacion> listar() {
        return cotizacionRepository.findAllByOrderByIdDesc();
    }

    @Transactional(readOnly = true)
    public List<Cotizacion> deSolicitud(Long solicitudId) {
        return cotizacionRepository.findBySolicitudIdOrderByIdDesc(solicitudId);
    }

    @Transactional(readOnly = true)
    public Cotizacion obtener(Long id) {
        return cotizacionRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Cotización", id));
    }

    public Cotizacion guardar(Cotizacion form) {
        Solicitud s = form.getSolicitud();
        if (s != null) {
            if (s.getEstado() != EstadoSolicitud.REGISTRADA && s.getEstado() != EstadoSolicitud.COTIZADA) {
                throw new NegocioException("La solicitud " + s.getCodigo() + " no admite nuevas cotizaciones (" + s.getEstado().getLabel() + ")");
            }
            form.setCliente(s.getCliente());
        }
        if (form.isNuevo()) {
            form.setEstado(EstadoCotizacion.BORRADOR);
            Cotizacion c = cotizacionRepository.save(form);
            c.setCodigo(Entidades.codigo("COT", c.getId()));
            if (s != null) {
                sugerirDetalle(c, s);
            }
            return c;
        }
        Cotizacion c = obtener(form.getId());
        exigirEditable(c);
        Entidades.copiar(form, c, "codigo", "estado", "subtotal", "igv", "total", "detalles");
        return c;
    }

    /** Propone una línea a partir de la solicitud usando la tarifa promedio del tipo de unidad. */
    private void sugerirDetalle(Cotizacion c, Solicitud s) {
        if (s.getTipoUnidad() == null) {
            return;
        }
        TipoUnidad tipo = s.getTipoUnidad();
        List<Unidad> unidades = unidadRepository.findAllByOrderByCodigoAsc().stream().filter(u -> u.getTipo() == tipo).toList();
        long dias = ChronoUnit.DAYS.between(s.getFechaInicio(), s.getFechaFin()) + 1;
        BigDecimal tarifa = promedio(unidades.stream().map(Unidad::getTarifaDia).filter(Objects::nonNull).toList());
        Modalidad modalidad = s.getTipoServicio() == TipoServicio.ALQUILER ? Modalidad.POR_DIA : Modalidad.POR_VIAJE;
        BigDecimal cantidad = modalidad == Modalidad.POR_DIA
                ? BigDecimal.valueOf(dias * s.getCantidadUnidades()) : BigDecimal.valueOf(s.getCantidadUnidades());
        agregarLinea(c, s.getTipoServicio().getLabel() + " - " + tipo.getLabel()
                + (s.getOrigen() != null ? " (" + s.getOrigen() + (s.getDestino() != null ? " → " + s.getDestino() : "") + ")" : ""),
                tipo, modalidad, cantidad, tarifa);
    }

    private static BigDecimal promedio(List<BigDecimal> valores) {
        if (valores.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return valores.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(valores.size()), 2, RoundingMode.HALF_UP);
    }

    public void agregarDetalle(Long cotizacionId, String descripcion, TipoUnidad tipoUnidad, Modalidad modalidad,
                               BigDecimal cantidad, BigDecimal precio) {
        Cotizacion c = obtener(cotizacionId);
        exigirEditable(c);
        if (descripcion == null || descripcion.isBlank() || modalidad == null) {
            throw new NegocioException("Ingrese la descripción y la modalidad de cobro");
        }
        if (cantidad == null || cantidad.signum() <= 0 || precio == null || precio.signum() < 0) {
            throw new NegocioException("Ingrese una cantidad mayor que cero y un precio válido");
        }
        agregarLinea(c, descripcion, tipoUnidad, modalidad, cantidad, precio);
    }

    private void agregarLinea(Cotizacion c, String descripcion, TipoUnidad tipo, Modalidad modalidad,
                              BigDecimal cantidad, BigDecimal precio) {
        CotizacionDetalle d = new CotizacionDetalle();
        d.setCotizacion(c);
        d.setDescripcion(descripcion.length() > 300 ? descripcion.substring(0, 300) : descripcion);
        d.setTipoUnidad(tipo);
        d.setModalidad(modalidad);
        d.setCantidad(cantidad);
        d.setPrecioUnitario(precio);
        d.setSubtotal(Montos.multiplicar(cantidad, precio));
        c.getDetalles().add(d);
        recalcular(c);
    }

    public void quitarDetalle(Long cotizacionId, Long detalleId) {
        Cotizacion c = obtener(cotizacionId);
        exigirEditable(c);
        c.getDetalles().removeIf(d -> d.getId().equals(detalleId));
        recalcular(c);
    }

    public void enviar(Long id) {
        Cotizacion c = obtener(id);
        exigirEditable(c);
        if (c.getDetalles().isEmpty() || c.getTotal().signum() <= 0) {
            throw new NegocioException("La cotización debe tener al menos una línea con importe antes de enviarse");
        }
        c.setEstado(EstadoCotizacion.ENVIADA);
        if (c.getSolicitud() != null) {
            c.getSolicitud().setEstado(EstadoSolicitud.COTIZADA);
        }
    }

    public void aceptar(Long id) {
        Cotizacion c = obtener(id);
        if (c.getEstado() != EstadoCotizacion.ENVIADA) {
            throw new NegocioException("Solo se pueden aceptar cotizaciones enviadas");
        }
        if (c.getFechaVencimiento().isBefore(LocalDate.now())) {
            c.setEstado(EstadoCotizacion.VENCIDA);
            throw new NegocioException("La cotización venció el " + c.getFechaVencimiento() + "; emita una nueva");
        }
        c.setEstado(EstadoCotizacion.ACEPTADA);
        if (c.getSolicitud() != null) {
            c.getSolicitud().setEstado(EstadoSolicitud.APROBADA);
        }
    }

    public void rechazar(Long id) {
        Cotizacion c = obtener(id);
        if (c.getEstado() != EstadoCotizacion.ENVIADA && c.getEstado() != EstadoCotizacion.BORRADOR) {
            throw new NegocioException("La cotización ya está " + c.getEstado().getLabel().toLowerCase());
        }
        c.setEstado(EstadoCotizacion.RECHAZADA);
    }

    private void recalcular(Cotizacion c) {
        BigDecimal subtotal = c.getDetalles().stream().map(CotizacionDetalle::getSubtotal).reduce(BigDecimal.ZERO, BigDecimal::add);
        c.setSubtotal(Montos.redondear(subtotal));
        c.setIgv(Montos.igv(subtotal));
        c.setTotal(c.getSubtotal().add(c.getIgv()));
    }

    private void exigirEditable(Cotizacion c) {
        if (!c.isEditable()) {
            throw new NegocioException("La cotización " + c.getCodigo() + " ya no se puede modificar (" + c.getEstado().getLabel() + ")");
        }
    }
}
