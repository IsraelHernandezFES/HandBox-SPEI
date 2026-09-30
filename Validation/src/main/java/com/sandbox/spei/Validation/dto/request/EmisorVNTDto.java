package com.sandbox.spei.Validation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmisorVNTDto {

    @NotNull(message = "PRX-003: Institucion inexistente en el catalogo")
    private String institucion;

    // Regla V18 (PRX-011)
    @NotBlank(message = "PRX-011: El nombre del ordenante es obligatorio")
    @Size(min = 1, max = 40, message = "El nombre del ordenante debe tener entre 1 y 40 caracteres")
    private String nombre;

    // --- REGLA V16: Obligatorios en VNT (PRX-011) ---
    @NotBlank(message = "PRX-011: La sucursal donde se realizo el deposito es obligatoria en VNT")
    private String sucursal;

    @NotNull(message = "PRX-011: El documento de identidad es obligatorio en VNT")
    private DocumentoIdentidadDto documentoIdentidad;

    // --- REGLA V17: Prohibidos en VNT (PRX-012) ---
    @Null(message = "PRX-012: El campo cuenta no debe venir en operaciones VNT")
    private String cuenta;
}