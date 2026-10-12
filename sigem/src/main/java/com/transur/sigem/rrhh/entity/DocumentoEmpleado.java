package com.transur.sigem.rrhh.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "documentos_empleado")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoEmpleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "tipo_documento", nullable = false, length = 100)
    private String tipoDocumento; // LICENCIA_AIIIC, SCTR_SALUD, CERTIFICACION_EXCAVADORA

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;

    @Column(name = "fecha_emision")
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento; // CRÍTICO para alertas de vencimiento

    @Column(name = "ruta_archivo", length = 255)
    private String rutaArchivo; // URL de Cloudinary o ruta local

    @Column(nullable = false)
    private Boolean estado = true; // VIGENTE, VENCIDO, RENOVANDO
}