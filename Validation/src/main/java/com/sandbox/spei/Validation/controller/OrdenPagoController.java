package com.sandbox.spei.Validation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/operaciones")
public class OrdenPagoController {

    private final OrdenPagoService ordenPagoService;
    private final ObjectMapper objectMapper;
    private final Validator validator;

    public OrdenPagoController(OrdenPagoService ordenPagoService,
                               ObjectMapper objectMapper,
                               Validator validator) {
        this.ordenPagoService = ordenPagoService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    @GetMapping
    public ResponseEntity<Page<OperacionResponse>> listarOperaciones(
            @ParameterObject @PageableDefault(size = 20, page = 0, sort = "fecha", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(ordenPagoService.obtenerTodasLasOrdenes(pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OperacionResponse> buscarPorId(@PathVariable String id) {
        return ordenPagoService.obtenerPorReferencia(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<OperacionResponse> crearOperacion(
            @RequestBody Map<String, Object> requestBody,
            @RequestHeader(value = "Clave-Idempotencia", required = false) String claveIdempotencia,
            @RequestHeader(value = "X-Escenario-Forzado", required = false) String escenarioForzado) {

        String tipoOperacion = (String) requestBody.get("tipoOperacion");
        OperacionResponse respuesta;

        if ("VNT".equalsIgnoreCase(tipoOperacion)) {
            OperacionVNTRequest requestVnt = objectMapper.convertValue(requestBody, OperacionVNTRequest.class);
            validarRequest(requestVnt);
            respuesta = ordenPagoService.procesarOperacionVNT(requestVnt, claveIdempotencia, escenarioForzado);
        } else if ("T2T".equalsIgnoreCase(tipoOperacion)) {
            OperacionT2TRequest requestT2t = objectMapper.convertValue(requestBody, OperacionT2TRequest.class);
            validarRequest(requestT2t);
            respuesta = ordenPagoService.procesarOperacionT2T(requestT2t, claveIdempotencia, escenarioForzado);
        } else {
            throw new com.sandbox.spei.Validation.exception.SpeiException(
                    "PRX-031",
                    "El tipo de operacion debe ser estrictamente T2T o VNT",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        if (respuesta.isFromCache()) {
            return ResponseEntity.ok(respuesta);
        } else {
            return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
        }
    }

    @PatchMapping("/{id}/estado")
    public ResponseEntity<OperacionResponse> cambiarEstado(
            @PathVariable Long id,
            @RequestParam String nuevoEstado) {
        OperacionResponse response = ordenPagoService.actualizarEstado(id, nuevoEstado);
        return ResponseEntity.ok(response);
    }

    private void validarRequest(Object request) {
        Set<ConstraintViolation<Object>> violaciones = validator.validate(request);
        if (!violaciones.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (ConstraintViolation<Object> v : violaciones) {
                sb.append(v.getMessage()).append("; ");
            }
            throw new IllegalArgumentException("Error de validación: " + sb.toString());
        }
    }
}