package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.exception.SpeiException;
import com.sandbox.spei.Validation.repository.OrdenPagoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class IdempotenciaService {

    private final OrdenPagoRepository ordenPagoRepository;


    public IdempotenciaService(OrdenPagoRepository ordenPagoRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
    }

    // Valida la idempotencia de la operación mediante el encabezado de clave de idempotencia.
     // Si la clave ya fue registrada, lanza una SpeiException con el código PRX-015 y estatus 409 (CONFLICT).
    public void validarIdempotencia(String claveIdempotencia) {
        if (claveIdempotencia != null && !claveIdempotencia.isBlank()) {
            if (ordenPagoRepository.existsByClaveRastreo(claveIdempotencia)) { // O el método de repositorio correspondiente para claves de idempotencia
                throw new SpeiException(
                        "PRX-015",
                        "La clave de idempotencia '" + claveIdempotencia + "' ya fue registrada previamente.",
                        HttpStatus.CONFLICT
                );
            }
        }
    }
}