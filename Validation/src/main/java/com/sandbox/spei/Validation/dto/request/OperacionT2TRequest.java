package com.sandbox.spei.Validation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
@JsonIgnoreProperties (ignoreUnknown = true)
public class OperacionT2TRequest {
    private String tipoOperacion;

    @NotBlank(message = "PRX-009:La referencia de seguimiento es obligatoria, alfanumerica, de 1 a 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9]{1,30}$", message = "PRX-009:La referencia de seguimiento es obligatoria, alfanumerica, de 1 a 30 caracteres")
    private String referenciaSeguimiento;

    @Valid @NotNull private ImporteDto importe;
    @Valid @NotNull private EmisorT2TDto emisor;
    @Valid @NotNull private ReceptorDto receptor;

    @NotBlank(message = "PRX-007:El concepto es obligatorio, de 1 a 40 caracteres")
    @Size(min = 1, max = 40, message = "PRX-007:El concepto es obligatorio, de 1 a 40 caracteres")
    private String concepto;

    @NotNull(message = "PRX-008:El folio numerico debe ser un entero entre 1 y 9,999,999")
    @Min(value = 1, message = "PRX-008:El folio numerico debe ser un entero entre 1 y 9,999,999")
    @Max(value = 9999999, message = "PRX-008:El folio numerico debe ser un entero entre 1 y 9,999,999")
    private Integer folioNumerico;
}