package com.transur.sigem.cliente.entity;

import com.transur.sigem.security.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "clientes")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@NoArgsConstructor
@AllArgsConstructor
public class Cliente {

    @EqualsAndHashCode.Include
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN: Un cliente puede tener un usuario asociado (para login en portal)
    // UNIQUE = true porque en tu SQL es BIGINT UNIQUE
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id", unique = true)
    private Usuario usuario;

    @Column(nullable = false, length = 20)
    private String tipoCliente; // NATURAL o JURIDICA (Validado por CHECK en BD)

    @Column(name = "razon_social", nullable = false, length = 200)
    private String razonSocial;

    @Column(nullable = false, unique = true, length = 30)
    private String documento; // RUC o DNI

    @Column(length = 20)
    private String telefono;

    @Column(length = 100)
    private String email;

    @Column(columnDefinition = "TEXT")
    private String direccion;

    @Column(nullable = false)
    private Boolean estado = true;

    @Column(nullable = false)
    private LocalDateTime fechaRegistro = LocalDateTime.now();

    // RELACIÓN: Un cliente tiene muchos contactos
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ContactoCliente> contactos;

    @Column(name = "logo_url", length = 500)
    private String logoUrl; // URL HTTPS del logo de la empresa o foto del contacto
}