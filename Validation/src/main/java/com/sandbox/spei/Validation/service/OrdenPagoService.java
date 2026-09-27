package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;

import java.util.List;
import java.util.Optional;

public interface OrdenPagoService {

    OperacionResponse procesarOperacionT2T(OperacionT2TRequest request, String claveIdempotencia);

    OperacionResponse procesarOperacionVNT(OperacionVNTRequest request, String claveIdempotencia);

    Optional<OperacionResponse> obtenerPorReferencia(String referencia);

    List<OperacionResponse> obtenerTodasLasOrdenes();
}