package com.sandbox.spei.Validation.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrdenLogResponse {
    private Long id;
    private short step;
    private String cveRastreo;
    private String estado;
    private LocalDateTime horaProcesamiento;
    private String motivo;
    private String detalle;
}