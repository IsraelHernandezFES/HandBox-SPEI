package com.sandbox.spei.Validation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "estado")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Estado {

    @Id
    @Column(name = "codigo", nullable = false)
    private Short codigo;

    @Column(name = "cve", length = 30, nullable = false, unique = true)
    private String cve;

    @Column(name = "nombre", length = 100, nullable = false, unique = true)
    private String nombre;

    @Column(name = "descripcion", length = 255)
    private String descripcion;
}
