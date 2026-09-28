package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/operaciones")
public class OrdenPagoController {

    private final OrdenPagoService ordenPagoService;

    public OrdenPagoController(OrdenPagoService ordenPagoService) {
        this.ordenPagoService = ordenPagoService;
    }

    // 1. Listar el historial paginado por fecha de registro descendente
    @GetMapping
    public ResponseEntity<Object> listarOperaciones(
            @RequestParam(defaultValue = "0") int pagina,
            @RequestParam(defaultValue = "20") int tamano) {
        // Aquí puedes adaptar el servicio para retornar la página de operaciones según el contrato
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
            @Valid @RequestBody Map<String, Object> requestBody,
            @RequestHeader(value = "Clave-Idempotencia", required = false) String claveIdempotencia,
            @RequestHeader(value = "X-Escenario-Forzado", required = false) String escenarioForzado) {

        String tipoOperacion = (String) requestBody.get("tipoOperacion");
        OperacionResponse respuesta;

        // Lógica de ruteo interno basada en el tipo de operación del contrato
        if ("VNT".equalsIgnoreCase(tipoOperacion)) {
            // Mapeo o procesamiento para VNT
            respuesta = ordenPagoService.procesarOperacionVNT(null, claveIdempotencia);
        } else {
            // Por defecto T2T
            respuesta = ordenPagoService.procesarOperacionT2T(null, claveIdempotencia);
        }

        // El contrato exige responder siempre con 201 Created para instrucciones bien formadas
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}