package com.sandbox.spei.Validation.dto.request;

import com.sandbox.spei.Validation.validator.ValidClabe;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

// Maneja depósitos en efectivo en ventanilla, no lleva cuenta ordenante, pero exige sucursal y la INE del depositante

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperacionVNTRequest {

    // PRX-009: La referencia de seguimiento es obligatoria, alfanumérica, de 1 a 30 caracteres
    @NotBlank(message = "PRX-009:La referencia de seguimiento es obligatoria, alfanumerica, de 1 a 30 caracteres")
    @Pattern(regexp = "^[a-zA-Z0-9]{1,30}$", message = "PRX-009:La referencia de seguimiento es obligatoria, alfanumerica, de 1 a 30 caracteres")
    private String referenciaSeguimiento;

    // PRX-004 y PRX-005: Mayor a cero, máximo 1,000,000.00 y máximo 2 decimales
    @NotNull(message = "PRX-004:El importe debe ser mayor que cero")
    @DecimalMin(value = "0.01", message = "PRX-004:El importe debe ser mayor que cero")
    @DecimalMax(value = "1000000.00", message = "PRX-005:El importe excede 1,000,000.00 o tiene mas de dos decimales")
    @Digits(integer = 7, fraction = 2, message = "PRX-005:El importe excede 1,000,000.00 o tiene mas de dos decimales")
    private BigDecimal monto;

    // PRX-006: La divisa debe ser MXN
    @NotBlank(message = "PRX-006:La divisa debe ser MXN")
    @Pattern(regexp = "^MXN$", message = "PRX-006:La divisa debe ser MXN")
    @Builder.Default
    private String divisa = "MXN";

    // PRX-007: El concepto es obligatorio, de 1 a 40 caracteres
    @NotBlank(message = "PRX-007:El concepto es obligatorio, de 1 a 40 caracteres")
    @Size(min = 1, max = 40, message = "PRX-007:El concepto es obligatorio, de 1 a 40 caracteres")
    private String concepto;

    // PRX-008: El folio numérico debe ser un entero entre 1 y 9,999,999
    @NotNull(message = "PRX-008:El folio numerico debe ser un entero entre 1 y 9,999,999")
    @Min(value = 1, message = "PRX-008:El folio numerico debe ser un entero entre 1 y 9,999,999")
    @Max(value = 9999999, message = "PRX-008:El folio numerico debe ser un entero entre 1 y 9,999,999")
    private Integer folioNumerico;

    // Datos del Ordenante en Ventanilla (No hay cuenta ordenante)
    @NotBlank(message = "El nombre del ordenante es obligatorio")
    @Size(max = 150, message = "El nombre del ordenante no debe exceder 150 caracteres")
    private String nombreOrdenante;

    @NotBlank(message = "El tipo de documento es obligatorio")
    @Size(max = 50, message = "El tipo de documento no debe exceder 50 caracteres")
    private String tipoDocumento;

    @NotBlank(message = "El numero de documento es obligatorio")
    @Size(max = 50, message = "El numero de documento no debe exceder 50 caracteres")
    private String numeroDocumento;

    @NotBlank(message = "La sucursal donde se realizo el deposito es obligatoria")
    @Size(max = 100, message = "El nombre de la sucursal no debe exceder 100 caracteres")
    private String sucursal;

    // Datos del Beneficiario
    @NotBlank(message = "El nombre del beneficiario es obligatorio")
    @Size(max = 150, message = "El nombre del beneficiario no debe exceder 150 caracteres")
    private String nombreBeneficiario;

    // PRX-001 y PRX-002: La cuenta debe tener exactamente 18 dígitos numéricos y pasar la prueba del dígito verificador
    @NotBlank(message = "PRX-001:La cuenta debe tener exactamente 18 digitos numericos")
    @Pattern(regexp = "^[0-9]{18}$", message = "PRX-001:La cuenta debe tener exactamente 18 digitos numericos")
    @ValidClabe(message = "PRX-002: Dígito verificador de la CLABE beneficiaria incorrecto")
    private String cuentaBeneficiaria;

    // PRX-003: Institución inexistente en el catálogo
    @NotNull(message = "PRX-003:Institucion inexistente en el catalogo")
    private Integer codigoInstitucion;
}