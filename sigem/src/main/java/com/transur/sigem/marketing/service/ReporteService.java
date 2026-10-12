package com.transur.sigem.marketing.service;

import com.transur.sigem.cliente.repository.ClienteRepository;
import com.transur.sigem.marketing.dto.ReporteMarketingDTO;
import com.transur.sigem.marketing.repository.LeadRepository;
import com.transur.sigem.marketing.repository.OportunidadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReporteService {

    private final LeadRepository leadRepository;
    private final OportunidadRepository oportunidadRepository;
    private final ClienteRepository clienteRepository; // ✅ Inyectado

    private static final String[] MESES = {"Ene", "Feb", "Mar", "Abr", "May", "Jun",
            "Jul", "Ago", "Sep", "Oct", "Nov", "Dic"};

    public List<ReporteMarketingDTO> obtenerReporteMensual(int anio) {
        List<ReporteMarketingDTO> reporte = new ArrayList<>();

        for (int mes = 1; mes <= 12; mes++) {
            // 1. Leads
            int totalLeads = leadRepository.countByFechaRegistroYearAndMonth(anio, mes);

            // 2. Monto Pipeline
            double totalMonto = oportunidadRepository.sumMontoByFechaCreacionYearAndMonth(anio, mes);

            // 3. Oportunidades Creadas (Conteo)
            int totalOpps = oportunidadRepository.countByFechaCreacionYearAndMonth(anio, mes);

            // 4. Clientes Nuevos
            int totalClientes = clienteRepository.countByFechaRegistroYearAndMonth(anio, mes);

            reporte.add(new ReporteMarketingDTO(
                    MESES[mes-1], totalLeads, totalMonto, totalOpps, totalClientes
            ));
        }
        return reporte;
    }
}