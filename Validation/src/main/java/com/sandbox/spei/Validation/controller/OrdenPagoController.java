package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenPagoController {

    private final OrdenPagoService ordenPagoService;

    public OrdenPagoController(OrdenPagoService ordenPagoService) {
        this.ordenPagoService = ordenPagoService;
    }

    // 1. Listar todas las órdenes
    @GetMapping
    public ResponseEntity<List<OperacionResponse>> listarTodas() {
        return ResponseEntity.ok(ordenPagoService.obtenerTodasLasOrdenes());
    }

    // 2. Buscar una orden por referencia de seguimiento / clave de rastreo
    @GetMapping("/rastreo/{clave}")
    public ResponseEntity<OperacionResponse> buscarPorClaveRastreo(@PathVariable String clave) {
        return ordenPagoService.obtenerPorReferencia(clave)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Alta de operación Tercero a Tercero (T2T)
    @PostMapping("/t2t")
    public ResponseEntity<OperacionResponse> crearOperacionT2T(
            @Valid @RequestBody OperacionT2TRequest request,
            @RequestHeader(value = "Clave-Idempotencia", required = false) String claveIdempotencia) {

        OperacionResponse respuesta = ordenPagoService.procesarOperacionT2T(request, claveIdempotencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }

    // 4. Alta de operación Ventanilla a Tercero (VNT)
    @PostMapping("/vnt")
    public ResponseEntity<OperacionResponse> crearOperacionVNT(
            @Valid @RequestBody OperacionVNTRequest request,
            @RequestHeader(value = "Clave-Idempotencia", required = false) String claveIdempotencia) {

        OperacionResponse respuesta = ordenPagoService.procesarOperacionVNT(request, claveIdempotencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(respuesta);
    }
}