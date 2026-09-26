package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.entity.OrdenLog;
import com.sandbox.spei.Validation.service.OrdenLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-log")
public class OrdenLogController {

    private final OrdenLogService ordenLogService;

    public OrdenLogController(OrdenLogService ordenLogService) {
        this.ordenLogService = ordenLogService;
    }

    @GetMapping("/orden/{ordenPagoId}")
    public ResponseEntity<List<OrdenLog>> listarLogsPorOrden(@PathVariable Long ordenPagoId) {
        List<OrdenLog> logs = ordenLogService.obtenerLogsOrdenadosPorPaso(ordenPagoId);
        if (logs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/rastreo/{clave}")
    public ResponseEntity<List<OrdenLog>> listarLogsPorClaveRastreo(@PathVariable String clave) {
        List<OrdenLog> logs = ordenLogService.obtenerLogsPorClaveRastreo(clave);
        if (logs.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(logs);
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrdenLog> buscarPorId(@PathVariable Long id) {
        return ordenLogService.obtenerLogPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}