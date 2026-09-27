package com.sandbox.spei.Validation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "registro_idempotencia")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegistroIdempotencia {

    @Id
    @Column(name = "clave_idempotencia", length = 100, nullable = false)
    private String claveIdempotencia;

    @Column(name = "request_hash", length = 64, nullable = false)
    private String requestHash;

    @Lob
    @Column(name = "response_body", columnDefinition = "TEXT", nullable = false)
    private String responseBody;

    @Column(name = "status_code", nullable = false)
    private Integer statusCode;

    @CreationTimestamp
    @Column(name = "fecha_creacion", updatable = false)
    private LocalDateTime fechaCreacion;
}