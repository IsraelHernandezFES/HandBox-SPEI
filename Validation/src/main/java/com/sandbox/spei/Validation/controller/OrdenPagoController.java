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

    // 2. Consultar una operación por ID (Caso A22 / A18-A20)
    @GetMapping("/{id}")
    public ResponseEntity<OperacionResponse> buscarPorId(@PathVariable String id) {
        return ordenPagoService.obtenerPorReferencia(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Alta unificada de operación (T2T o VNT según el JSON recibido)
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
            respuesta = ordenPagoService.procesarOperacionVNT(requestVnt, claveIdempotencia);
        } else {
            OperacionT2TRequest requestT2t = objectMapper.convertValue(requestBody, OperacionT2TRequest.class);
            validarRequest(requestT2t);
            respuesta = ordenPagoService.procesarOperacionT2T(requestT2t, claveIdempotencia);
        }

        // Si viene de la caché, devolvemos 200 OK
        if (respuesta.isFromCache()) {
            return ResponseEntity.ok(respuesta);
        }
        // Si es una operación nueva, devolvemos 201 Created
        else {
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

    // Método auxiliar para lanzar errores si las validaciones del DTO fallan
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