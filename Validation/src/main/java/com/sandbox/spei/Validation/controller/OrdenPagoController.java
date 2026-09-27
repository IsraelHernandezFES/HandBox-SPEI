package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.entity.OrdenPago;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import com.sandbox.spei.Validation.service.OrdenPagoServiceImpl;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenPagoController {

    private final OrdenPagoServiceImpl ordenPagoServiceImpl;
    private final OrdenPagoService ordenPagoService;

    public OrdenPagoController(OrdenPagoServiceImpl ordenPagoServiceImpl, OrdenPagoService ordenPagoService) {
        this.ordenPagoServiceImpl = ordenPagoServiceImpl;
        this.ordenPagoService = ordenPagoService;
    }

    // 1. Endpoint para listar todas las órdenes de pago (GET: /api/ordenes)
    @GetMapping
    public ResponseEntity<List<OrdenPago>> listarTodas() {
        List<OrdenPago> ordenes = ordenPagoService.obtenerTodasLasOrdenes();
        return ResponseEntity.ok(ordenes);
    }

    // 2. Endpoint para buscar una orden por Clave de Rastreo (GET: /api/ordenes/rastreo/{clave})
    @GetMapping("/rastreo/{clave}")
    public ResponseEntity<OrdenPago> buscarPorClaveRastreo(@PathVariable String clave) {
        return ordenPagoService.obtenerOrdenPorClaveRastreo(clave)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Endpoint para registrar una operación Cuenta a Cuenta (POST: /api/ordenes/t2t)
    @PostMapping("/t2t")
    public ResponseEntity<OrdenPago> crearOperacionT2T(
            @Valid @RequestBody OperacionT2TRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String claveIdempotencia) {

        OrdenPago ordenGuardada = ordenPagoServiceImpl.procesarT2T(request, claveIdempotencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenGuardada);
    }

    // 4. Endpoint para registrar una operación Ventanilla a Tercero (POST: /api/ordenes/vnt)
    @PostMapping("/vnt")
    public ResponseEntity<OrdenPago> crearOperacionVNT(
            @Valid @RequestBody OperacionVNTRequest request,
            @RequestHeader(value = "X-Idempotency-Key", required = false) String claveIdempotencia) {

        OrdenPago ordenGuardada = ordenPagoServiceImpl.procesarVNT(request, claveIdempotencia);
        return ResponseEntity.status(HttpStatus.CREATED).body(ordenGuardada);
    }
}