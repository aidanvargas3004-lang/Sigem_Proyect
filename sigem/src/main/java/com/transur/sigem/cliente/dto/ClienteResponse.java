package com.transur.sigem.cliente.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClienteResponse {
    private Long id;
    private String razonSocial;
    private String documento;
    private String email;
    private String telefono;
    private String direccion;
    private String tipoCliente;
    private Boolean estado;
}