package com.transmaqsur.sigem.service;

import com.transmaqsur.sigem.exception.NegocioException;
import com.transmaqsur.sigem.exception.NoEncontradoException;
import com.transmaqsur.sigem.model.Cliente;
import com.transmaqsur.sigem.model.Contacto;
import com.transmaqsur.sigem.model.enums.TipoDocumento;
import com.transmaqsur.sigem.repository.ClienteRepository;
import com.transmaqsur.sigem.repository.ContactoRepository;
import com.transmaqsur.sigem.util.Entidades;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ContactoRepository contactoRepository;

    @Transactional(readOnly = true)
    public List<Cliente> listar() {
        return clienteRepository.findAllByOrderByRazonSocialAsc();
    }

    @Transactional(readOnly = true)
    public List<Cliente> listarActivos() {
        return clienteRepository.findByActivoTrueOrderByRazonSocialAsc();
    }

    @Transactional(readOnly = true)
    public Cliente obtener(Long id) {
        return clienteRepository.findById(id).orElseThrow(() -> new NoEncontradoException("Cliente", id));
    }

    public Cliente guardar(Cliente form) {
        validarDocumento(form);
        Long id = form.isNuevo() ? 0L : form.getId();
        if (clienteRepository.existsByNumeroDocumentoAndIdNot(form.getNumeroDocumento(), id)) {
            throw new NegocioException("Ya existe un cliente con el documento " + form.getNumeroDocumento());
        }
        if (form.isNuevo()) {
            return clienteRepository.save(form);
        }
        Cliente c = obtener(form.getId());
        Entidades.copiar(form, c, "contactos");
        return c;
    }

    public void cambiarEstado(Long id) {
        Cliente c = obtener(id);
        c.setActivo(!c.isActivo());
    }

    private void validarDocumento(Cliente c) {
        int largo = c.getNumeroDocumento() == null ? 0 : c.getNumeroDocumento().length();
        boolean ok = switch (c.getTipoDocumento()) {
            case RUC -> largo == 11;
            case DNI -> largo == 8;
            case CE -> largo == 9;
        };
        if (!ok) {
            throw new NegocioException("El número de documento no corresponde al tipo " + c.getTipoDocumento().getLabel());
        }
        if (c.getTipoDocumento() != TipoDocumento.RUC && c.getTipoCliente() != null
                && c.getTipoCliente() != com.transmaqsur.sigem.model.enums.TipoCliente.PERSONA_NATURAL) {
            throw new NegocioException("Las empresas y gobiernos locales deben registrarse con RUC");
        }
    }

    // ------------------------------------------------------------ Contactos

    public Contacto agregarContacto(Long clienteId, Contacto contacto) {
        Cliente c = obtener(clienteId);
        contacto.setCliente(c);
        if (contacto.isPrincipal() || c.getContactos().isEmpty()) {
            c.getContactos().forEach(x -> x.setPrincipal(false));
            contacto.setPrincipal(true);
        }
        return contactoRepository.save(contacto);
    }

    public void eliminarContacto(Long clienteId, Long contactoId) {
        Contacto contacto = contactoRepository.findById(contactoId)
                .filter(x -> x.getCliente().getId().equals(clienteId))
                .orElseThrow(() -> new NoEncontradoException("Contacto", contactoId));
        Cliente c = contacto.getCliente();
        c.getContactos().remove(contacto);
        if (contacto.isPrincipal() && !c.getContactos().isEmpty()) {
            c.getContactos().get(0).setPrincipal(true);
        }
    }

    public void marcarPrincipal(Long clienteId, Long contactoId) {
        Cliente c = obtener(clienteId);
        c.getContactos().forEach(x -> x.setPrincipal(x.getId().equals(contactoId)));
    }

    @Transactional(readOnly = true)
    public List<Contacto> contactos(Long clienteId) {
        return contactoRepository.findByClienteIdOrderByNombresAsc(clienteId);
    }
}
