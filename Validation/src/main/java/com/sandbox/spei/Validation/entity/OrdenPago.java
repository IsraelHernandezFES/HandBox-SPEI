package com.sandbox.spei.Validation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orden_pago")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "clave_rastreo", nullable = false, unique = true, length = 50)
    private String claveRastreo;

    @Column(name = "monto", nullable = false, precision = 18, scale = 2)
    private BigDecimal monto;

    @Column(name = "moneda", nullable = false, length = 3)
    @Builder.Default
    private String moneda = "MXN";

    @CreationTimestamp
    @Column(name = "fecha", updatable = false)
    private LocalDateTime fecha;

    // Ordenante
    @Column(name = "nombre_ordenante", nullable = false, length = 150)
    private String nombreOrdenante;


    @Column(name = "cuenta_ordenante", length = 18) // Por defecto nullable = true
    private String cuentaOrdenante;


    // Beneficiario
    @Column(name = "nombre_beneficiario", nullable = false, length = 150)
    private String nombreBeneficiario;

    @Column(name = "cuenta_beneficiaria", nullable = false, length = 18)
    private String cuentaBeneficiaria;


    // Relaciones
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_institucion", nullable = false)
    private Institucion institucion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "codigo_tipo_operacion", nullable = false)
    private TipoOperacion tipoOperacion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_actual", nullable = false)
    private Estado estadoActual;
}