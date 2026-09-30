package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import org.springframework.data.domain.Page; // ¡Añadir este import!
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface OrdenPagoService {

    Optional<OperacionResponse> obtenerPorReferencia(String referencia);

    Page<OperacionResponse> obtenerTodasLasOrdenes(Pageable pageable);

    OperacionResponse actualizarEstado(Long id, String nuevoEstado);

    OperacionResponse procesarOperacionT2T(OperacionT2TRequest request, String claveIdempotencia, String escenarioForzado);

    OperacionResponse procesarOperacionVNT(OperacionVNTRequest request, String claveIdempotencia, String escenarioForzado);
}