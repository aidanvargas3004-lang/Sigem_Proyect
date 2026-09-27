package com.transmaqsur.sigem.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "proveedor")
public class Proveedor extends EntidadBase {

    @NotBlank
    @Pattern(regexp = "\\d{11}", message = "el RUC debe tener 11 dígitos")
    @Column(nullable = false, unique = true, length = 11)
    private String ruc;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String razonSocial;

    @Size(max = 80)
    @Column(length = 80)
    private String rubro;

    @Size(max = 120)
    @Column(length = 120)
    private String contacto;

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

    private boolean activo = true;
}
