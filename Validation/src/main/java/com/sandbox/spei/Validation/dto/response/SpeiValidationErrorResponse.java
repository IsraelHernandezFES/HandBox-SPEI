package com.sandbox.spei.Validation.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

//La prueba en Postman o en el script de evaluación no espera una lista suelta de textos; espera un objeto principal que contenga la referencia y una lista de errores
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpeiValidationErrorResponse {
    private String referenciaSeguimiento;
    private List<ItemErrorResponse> errores;
}

//Genera un JSON asi
//{
//  "referenciaSeguimiento": "PRX1790298374783209",
//  "errores": [
//    { ... }
//  ]
//}