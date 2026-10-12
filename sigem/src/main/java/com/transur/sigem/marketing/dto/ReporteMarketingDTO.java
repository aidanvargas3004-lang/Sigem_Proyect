package com.transur.sigem.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ReporteMarketingDTO {
    private String mes;
    private int leads;
    private double montoPipeline;      // Valor total de oportunidades creadas
    private int oportunidadesCreadas;  // Cantidad de opps nuevas
    private int clientesNuevos;        // Clientes registrados este mes
}