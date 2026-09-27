package com.transmaqsur.sigem.model;

import com.transmaqsur.sigem.model.enums.TipoCliente;
import com.transmaqsur.sigem.model.enums.TipoDocumento;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "cliente")
public class Cliente extends EntidadBase {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TipoDocumento tipoDocumento = TipoDocumento.RUC;

    @NotBlank
    @Pattern(regexp = "\\d{8}|\\d{9}|\\d{11}", message = "debe tener 8 (DNI), 9 (CE) u 11 (RUC) dígitos")
    @Column(nullable = false, unique = true, length = 11)
    private String numeroDocumento;

    @NotBlank
    @Size(max = 150)
    @Column(nullable = false, length = 150)
    private String razonSocial;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TipoCliente tipoCliente;

    @Size(max = 200)
    @Column(length = 200)
    private String direccion;

    @Size(max = 20)
    @Column(length = 20)
    private String telefono;

    @Email
    @Size(max = 120)
    @Column(length = 120)
    private String email;

    private boolean activo = true;

    @Size(max = 1000)
    @Column(length = 1000)
    private String observaciones;

    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("principal desc, nombres asc")
    private List<Contacto> contactos = new ArrayList<>();
}
