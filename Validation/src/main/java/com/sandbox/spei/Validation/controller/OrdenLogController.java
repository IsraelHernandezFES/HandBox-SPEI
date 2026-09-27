package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.entity.OrdenLog;
import com.sandbox.spei.Validation.service.OrdenLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordenes-logs")
public class OrdenLogController {

    private final OrdenLogService ordenLogService;

    public OrdenLogController(OrdenLogService ordenLogService) {
        this.ordenLogService = ordenLogService;
    }

    @GetMapping("/orden/{ordenPagoId}")
    public ResponseEntity<List<OrdenLog>> listarPorOrdenId(@PathVariable Long ordenPagoId) {
        return ResponseEntity.ok(ordenLogService.obtenerLogsPorOrden(ordenPagoId));
    }

    @GetMapping("/orden/{ordenPagoId}/ordenados")
    public ResponseEntity<List<OrdenLog>> listarOrdenadosPorPaso(@PathVariable Long ordenPagoId) {
        return ResponseEntity.ok(ordenLogService.obtenerLogsOrdenadosPorPaso(ordenPagoId));
    }

    @GetMapping("/rastreo/{cveRastreo}")
    public ResponseEntity<List<OrdenLog>> listarPorClaveRastreo(@PathVariable String cveRastreo) {
        return ResponseEntity.ok(ordenLogService.obtenerLogsPorClaveRastreo(cveRastreo));
    }
}