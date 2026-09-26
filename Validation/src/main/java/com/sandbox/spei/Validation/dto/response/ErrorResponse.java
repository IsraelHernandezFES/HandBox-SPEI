package com.sandbox.spei.Validation.dto.response;
//Estanderizar respuestas de error hacia el cliente y la suite de evualuacion

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@JsonInclude (JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private String codigo;
    private String mensaje;
    private LocalDateTime timestamp;
    private List<String> detalles;

}

//Ejemplo de Respuesta ante un error en JSON
//{
//  "codigo": "PRX-015",
//  "mensaje": "Clave de idempotencia duplicada con diferente payload",
//  "fecha": "2026-09-26T09:51:51"
//}