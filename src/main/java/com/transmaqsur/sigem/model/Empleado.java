package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.AreaEmpresa;
import com.transmaqsur.sigem.model.enums.CargoEmpleado;
import com.transmaqsur.sigem.model.enums.EstadoEmpleado;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@Entity
@Table(name = "empleado")
public class Empleado extends EntidadBase {

    @NotBlank
    @Pattern(regexp = "\\d{8}", message = "el DNI debe tener 8 dígitos")
    @Column(nullable = false, unique = true, length = 8)
    private String dni;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String nombres;

    @NotBlank
    @Size(max = 80)
    @Column(nullable = false, length = 80)
    private String apellidos;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private CargoEmpleado cargo;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private AreaEmpresa area;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    private LocalDate fechaIngreso;

    @Size(max = 20)
    @Column(length = 20)
    private String licenciaNumero;

    /** Categoría de licencia MTC (A-IIb, A-IIIc, etc.). */
    @Size(max = 10)
    @Column(length = 10)
    private String licenciaCategoria;

    private LocalDate licenciaVencimiento;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private EstadoEmpleado estado = EstadoEmpleado.ACTIVO;

    public String getNombreCompleto() {
        return nombres + " " + apellidos;
    }

    public boolean isPersonalDeCampo() {
        return cargo == CargoEmpleado.CONDUCTOR || cargo == CargoEmpleado.OPERADOR;
    }

    public boolean isLicenciaVencida() {
        return licenciaVencimiento != null && licenciaVencimiento.isBefore(LocalDate.now());
    }
}
