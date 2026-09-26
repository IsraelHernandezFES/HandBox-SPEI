package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.entity.OrdenPago;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes")
public class OrdenPagoController {

    private final OrdenPagoService ordenPagoService;

    // Inyección de dependencias del servicio
    public OrdenPagoController(OrdenPagoService ordenPagoService) {
        this.ordenPagoService = ordenPagoService;
    }

    // 1. Endpoint para obtener todas las órdenes (GET: /api/ordenes)
    @GetMapping
    public ResponseEntity<List<OrdenPago>> listarTodas() {
        List<OrdenPago> ordenes = ordenPagoService.obtenerTodasLasOrdenes();
        return ResponseEntity.ok(ordenes);
    }

    // 2. Endpoint para buscar una orden por su Clave de Rastreo (GET: /api/ordenes/rastreo/{clave})
    @GetMapping("/rastreo/{clave}")
    public ResponseEntity<OrdenPago> buscarPorClaveRastreo(@PathVariable String clave) {
        return ordenPagoService.obtenerOrdenPorClaveRastreo(clave)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. Endpoint para registrar una nueva orden de pago (POST: /api/ordenes)
    @PostMapping
    public ResponseEntity<OrdenPago> crearOrden(@RequestBody OrdenPago nuevaOrden) {
        OrdenPago ordenGuardada = ordenPagoService.guardarOrdenPago(nuevaOrden);
        return ResponseEntity.ok(ordenGuardada);
    }
}