package com.sandbox.spei.Validation.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "orden_log", uniqueConstraints = {
        @UniqueConstraint(name = "uk_orden_log_step", columnNames = {"orden_pago_id", "step"})
        })
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orden_pago_id", nullable = false)
    private OrdenPago ordenPago;

    @Column(name = "step", nullable = false)
    private Short step;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "estado_codigo", nullable = false)
    private Estado estado;

    @Column(name = "cve_rastreo", length = 100)
    private String cveRastreo;

    @CreationTimestamp
    @Column(name = "hora_procesamiento", updatable = false)
    private LocalDateTime horaProcesamiento;

    // Guardado como texto JSON
    @Column(name = "detalle", columnDefinition = "JSON")
    private String detalle;

    @Column(name = "motivo", length = 500)
    private String motivo;
}