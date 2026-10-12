package com.transur.sigem.publico.controller;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import com.transur.sigem.maquinaria.repository.MaquinariaRepository;
import com.transur.sigem.operaciones.entity.TipoServicio;
import com.transur.sigem.operaciones.repository.TipoServicioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class PublicController {
    private final MaquinariaRepository maquinariaRepository;
    private final TipoServicioRepository tipoServicioRepository;

    @GetMapping({"/", "/inicio-publico", "/catalogo"})
    public String home(Model model) {
        List<Maquinaria> maquinaria = maquinariaRepository.findAll().stream()
                .filter(m -> m.getEstadoOperativo() != null && !"BAJA_DEFINITIVA".equals(m.getEstadoOperativo()) && !"VENDIDO".equals(m.getEstadoOperativo()))
                .toList();
        List<TipoServicio> servicios = tipoServicioRepository.findAll().stream()
                .filter(t -> Boolean.TRUE.equals(t.getEstado()))
                .toList();
        model.addAttribute("maquinarias", maquinaria);
        model.addAttribute("tiposServicio", servicios);
        model.addAttribute("totalMaquinarias", maquinaria.size());
        model.addAttribute("totalServicios", servicios.size());
        return "public/home";
    }
}
