package com.transmaqsur.sigem.model;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Superclase de todas las entidades del negocio. Guarda quién y cuándo creó o
 * modificó cada registro (trazabilidad). Los cambios de detalle se registran
 * además en la tabla de auditoría.
 */
@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class EntidadBase {

    /** Campos que nunca se copian desde un formulario. */
    public static final String[] CAMPOS_SISTEMA = {"id", "fechaCreacion", "creadoPor", "fechaModificacion", "modificadoPor"};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime fechaCreacion;

    @CreatedBy
    @Column(length = 50, updatable = false)
    private String creadoPor;

    @LastModifiedDate
    private LocalDateTime fechaModificacion;

    @LastModifiedBy
    @Column(length = 50)
    private String modificadoPor;

    public boolean isNuevo() {
        return id == null;
    }
}
