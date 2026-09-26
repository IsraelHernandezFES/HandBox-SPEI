package com.sandbox.spei.Validation.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "tipo_operacion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TipoOperacion {

    @Id
    @Column(name = "codigo", length = 10, nullable = false)
    private String codigo;

    @Column(name = "nombre", nullable = false, length = 50, unique = true)
    private String nombre;

    @Column(name = "descripcion", nullable = false, length = 255)
    private String descripcion;

    @Lob
    @Column(name = "particularidad_estructural", columnDefinition = "TEXT", nullable = false)
    private String particularidadEstructural;
}