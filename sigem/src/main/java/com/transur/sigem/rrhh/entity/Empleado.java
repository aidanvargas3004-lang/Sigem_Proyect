package com.transur.sigem.rrhh.entity;

import com.transur.sigem.security.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "empleados")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Empleado {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN OPCIONAL: Un empleado puede no tener usuario (ej. personal de campo)
    // UNIQUE = true porque en tu SQL es BIGINT UNIQUE
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(nullable = false, unique = true, length = 30)
    private String documento; // DNI, CUIT, etc.

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, length = 100)
    private String cargo; // CONDUCTOR, OPERADOR, TECNICO, SUPERVISOR

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso;

    @Column(nullable = false)
    private Boolean estado = true;

    // RELACIÓN: Un empleado tiene muchos documentos (licencias, certificados, SCTR)
    @OneToMany(mappedBy = "empleado", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DocumentoEmpleado> documentos;

    @Column(name = "foto_perfil_url", length = 500)
    private String fotoPerfilUrl;

    @Column(name = "licencia_url", length = 500)
    private String licenciaUrl; // Licencia de conducir escaneada
}