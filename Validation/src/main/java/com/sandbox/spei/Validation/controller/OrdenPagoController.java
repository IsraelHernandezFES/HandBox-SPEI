package com.sandbox.spei.Validation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
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
    private final Validator validator; // Para disparar las validaciones de los DTOs manualmente si usas Map

    public OrdenPagoController(OrdenPagoService ordenPagoService,
                               ObjectMapper objectMapper,
                               Validator validator) {
        this.ordenPagoService = ordenPagoService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }

    // 1. Listar el historial paginado por fecha de registro descendente
    @GetMapping
    public ResponseEntity<Object> listarOperaciones(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        return ResponseEntity.ok(ordenPagoService.obtenerTodasLasOrdenes());
    }

    // 2. Consultar una operación con su historial de transiciones por ID
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
            // Mapeamos el Map al DTO de Ventanilla
            OperacionVNTRequest requestVnt = objectMapper.convertValue(requestBody, OperacionVNTRequest.class);

            // Validamos las anotaciones del DTO manualmente (opcional pero recomendado)
            validarRequest(requestVnt);

            // ¡Pasamos el objeto con datos al servicio!
            respuesta = ordenPagoService.procesarOperacionVNT(requestVnt, claveIdempotencia);
        } else {
            // Mapeamos el Map al DTO de T2T por defecto
            OperacionT2TRequest requestT2t = objectMapper.convertValue(requestBody, OperacionT2TRequest.class);

            // Validamos las anotaciones del DTO manualmente
            validarRequest(requestT2t);

            // ¡Pasamos el objeto con datos al servicio!
            respuesta = ordenPagoService.procesarOperacionT2T(requestT2t, claveIdempotencia);
        }

        // El contrato exige responder siempre con 201 Created para instrucciones bien formadas
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
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