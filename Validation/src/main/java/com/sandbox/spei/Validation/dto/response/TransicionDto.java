package com.sandbox.spei.Validation.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TransicionDto {
    private String estado;
    private LocalDateTime momento;
    private String motivo;
}