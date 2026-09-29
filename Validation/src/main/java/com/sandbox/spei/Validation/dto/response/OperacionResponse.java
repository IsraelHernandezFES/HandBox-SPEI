package com.sandbox.spei.Validation.dto.response;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

//Dto de respuesta comun para ambas operaciones (para retornar tras crearlas con 201 created o al consultarlas)

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OperacionResponse {

    private Long id;
    private String claveRastreo;
    private String tipoOperacion;
    private BigDecimal monto;
    private String moneda;
    private String estadoActual;
    private LocalDateTime fechaCreacion;
    private String nombreOrdenante;
    private String cuentaOrdenante;
    private String nombreBeneficiario;
    private String cuentaBeneficiaria;
    private Integer codigoInstitucion;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String codigoMotivo;
}