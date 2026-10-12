package com.transur.sigem.sistema.entity;

import com.transur.sigem.security.entity.Usuario;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "notificaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN: Una notificación pertenece a un usuario específico
    // ON DELETE CASCADE: Si borras al usuario, sus notificaciones también se borran (son efímeras)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario;

    @Column(nullable = false, length = 150)
    private String titulo; // Ej: "Vencimiento de Licencia", "Stock Mínimo Alcanzado"

    @Column(nullable = false, columnDefinition = "TEXT")
    private String mensaje; // Detalle completo de la alerta

    @Column(length = 30)
    private String tipo; // INFO, WARNING, ERROR, SUCCESS (Para estilos visuales en Thymeleaf)

    @Column(nullable = false)
    private Boolean leida = false;

    /** Identifica la alerta automática que la generó (evita duplicados). */
    @Column(length = 150)
    private String clave;

    /** Enlace interno a la pantalla relacionada. */
    @Column(length = 200)
    private String enlace;

    @Column(nullable = false)
    private LocalDateTime fecha = LocalDateTime.now();
}