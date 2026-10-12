package com.transur.sigem.security.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rol_permisos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolPermiso {

    @EmbeddedId
    private RolPermisoId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("rolId") // Mapea la parte 'rol_id' de la PK compuesta
    @JoinColumn(name = "rol_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rol_permisos_rol"))
    private Rol rol;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("permisoId") // Mapea la parte 'permiso_id' de la PK compuesta
    @JoinColumn(name = "permiso_id", nullable = false, foreignKey = @ForeignKey(name = "fk_rol_permisos_permiso"))
    private Permiso permiso;
}