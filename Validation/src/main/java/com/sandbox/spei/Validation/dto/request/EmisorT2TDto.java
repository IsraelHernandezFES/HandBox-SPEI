package com.sandbox.spei.Validation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sandbox.spei.Validation.validator.ValidClabe;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @JsonIgnoreProperties(ignoreUnknown = true)

public class EmisorT2TDto {
    @NotNull(message = "PRX-003:Institucion inexistente en el catalogo")
    private String institucion;

    @NotBlank(message = "PRX-001:La cuenta debe tener exactamente 18 digitos numericos")
    @Pattern(regexp = "^[0-9]{18}$", message = "PRX-001:La cuenta debe tener exactamente 18 digitos numericos")
    @ValidClabe(message = "PRX-002: Dígito verificador de la CLABE ordenante incorrecto")
    private String cuenta;

    @NotBlank(message = "El nombre del ordenante es obligatorio")
    @Size(max = 150, message = "El nombre del ordenante no debe exceder 150 caracteres")
    private String nombre;

    private String identificacionFiscal;
}