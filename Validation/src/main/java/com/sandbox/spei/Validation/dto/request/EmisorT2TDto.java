package com.sandbox.spei.Validation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sandbox.spei.Validation.validator.ValidClabe;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties(ignoreUnknown = true)
public class EmisorT2TDto {

    @NotNull(message = "PRX-003: Institucion inexistente en el catalogo")
    private String institucion;

    // Regla V14 (PRX-011) y V01 (PRX-001)
    @NotBlank(message = "PRX-011: La cuenta ordenante es obligatoria en operaciones T2T")
    @Pattern(regexp = "^[0-9]{18}$", message = "PRX-001: La cuenta debe tener exactamente 18 digitos numericos")
    @ValidClabe(message = "PRX-002: Digito verificador de la CLABE ordenante incorrecto")
    private String cuenta;

    // Regla V18 (PRX-011)
    @NotBlank(message = "PRX-011: El nombre del ordenante es obligatorio")
    @Size(min = 1, max = 40, message = "El nombre del ordenante debe tener entre 1 y 40 caracteres")
    private String nombre;

    private String identificacionFiscal;

    // --- REGLA V15: Prohibidos en T2T (PRX-012) ---
    @Null(message = "PRX-012: El campo sucursal no debe venir en operaciones T2T")
    private String sucursal;

    @Null(message = "PRX-012: El campo documentoIdentidad no debe venir en operaciones T2T")
    private Object documentoIdentidad;
}