package com.sandbox.spei.Validation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter @Setter @NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)

public class DocumentoIdentidadDto {
    @NotBlank(message = "El tipo de documento es obligatorio")
    private String tipo;

    @NotBlank(message = "El numero de documento es obligatorio")
    private String numero;
}