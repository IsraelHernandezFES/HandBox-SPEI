package com.sandbox.spei.Validation.exception;

//Excepcion personalizada de runtime para lanzar cualquier codigo del catalogo de negocio SPEI

import lombok.Getter;
import org.springframework.http.HttpStatus;

import java.util.List;

@Getter
public class SpeiException extends  RuntimeException{

    private final String codigo;
    private final HttpStatus httpStatus;
    private final List<String> detalles;

    public SpeiException(String codigo, String mensaje ,  HttpStatus httpStatus) {
        super (mensaje);
        this.codigo = codigo;
        this.httpStatus = httpStatus;
        this.detalles = null;

    }

    public SpeiException(String codigo, String mensaje, HttpStatus httpStatus, List<String> detalles) {
        super(mensaje);
        this.codigo = codigo;
        this.httpStatus = httpStatus;
        this.detalles = detalles;
    }

}
