package com.sandbox.spei.Validation.dto.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.*;
import lombok.*;
import java.math.BigDecimal;

@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder @JsonIgnoreProperties(ignoreUnknown = true)

public class ImporteDto {
    @NotNull(message = "PRX-004:El importe debe ser mayor que cero")
    @DecimalMin(value = "0.01", message = "PRX-004:El importe debe ser mayor que cero")
    @DecimalMax(value = "1000000.00", message = "PRX-005:El importe excede 1,000,000.00 o tiene mas de dos decimales")
    @Digits(integer = 7, fraction = 2, message = "PRX-005:El importe excede 1,000,000.00 o tiene mas de dos decimales")
    private BigDecimal valor;

    @NotBlank(message = "PRX-006:La divisa debe ser MXN")
    @Pattern(regexp = "^MXN$", message = "PRX-006:La divisa debe ser MXN")
    private String divisa;
}