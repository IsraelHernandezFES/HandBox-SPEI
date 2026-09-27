package com.sandbox.spei.Validation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "institucion")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Institucion {

    @Id
    @Column(name = "codigo", nullable = false)
    private Integer codigo;

    @Column(name = "nombre", nullable = false, length = 150, unique = true)
    private String nombre;

    @Column(name = "estado_operativo", nullable = false, length = 20)
    @Builder.Default
    private String estadoOperativo = "NORMAL";

    @CreationTimestamp
    @Column(name = "fecha_registro", updatable = false)
    private LocalDateTime fechaRegistro;

    @UpdateTimestamp
    @Column(name = "fecha_actualizacion")
    private LocalDateTime fechaActualizacion;
}