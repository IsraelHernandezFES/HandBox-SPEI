package com.sandbox.spei.Validation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @JsonIgnoreProperties(ignoreUnknown = true)

public class EmisorVNTDto {
    @NotNull(message = "PRX-003:Institucion inexistente en el catalogo")
    private String institucion;

    @NotBlank(message = "La sucursal donde se realizo el deposito es obligatoria")
    private String sucursal;

    @NotBlank(message = "El nombre del ordenante es obligatorio")
    private String nombre;

    @NotNull(message = "El documento de identidad es obligatorio")
    private DocumentoIdentidadDto documentoIdentidad;
}

