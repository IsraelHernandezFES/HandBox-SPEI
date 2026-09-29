package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.dto.response.OrdenLogResponse;
import com.sandbox.spei.Validation.service.OrdenLogService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/operaciones-logs")
public class OrdenLogController {

    private final OrdenLogService ordenLogService;

    public OrdenLogController(OrdenLogService ordenLogService) {
        this.ordenLogService = ordenLogService;
    }

    @GetMapping("/orden/{ordenPagoId}")
    public ResponseEntity<List<OrdenLogResponse>> listarPorOrdenId(@PathVariable Long ordenPagoId) {
        return ResponseEntity.ok(ordenLogService.obtenerLogsPorOrden(ordenPagoId));
    }

    @GetMapping("/orden/{ordenPagoId}/ordenados")
    public ResponseEntity<List<OrdenLogResponse>> listarOrdenadosPorPaso(@PathVariable Long ordenPagoId) {
        return ResponseEntity.ok(ordenLogService.obtenerLogsOrdenadosPorPaso(ordenPagoId));
    }

    @GetMapping("/rastreo/{cveRastreo}")
    public ResponseEntity<List<OrdenLogResponse>> listarPorClaveRastreo(@PathVariable String cveRastreo) {
        return ResponseEntity.ok(ordenLogService.obtenerLogsPorClaveRastreo(cveRastreo));
    }
}