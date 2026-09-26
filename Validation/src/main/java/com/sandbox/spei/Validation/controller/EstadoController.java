package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.entity.Estado;
import com.sandbox.spei.Validation.service.EstadoService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/estados")
public class EstadoController {

    private final EstadoService estadoService;

    public EstadoController(EstadoService estadoService) {
        this.estadoService = estadoService;
    }

  // catalogo de estados
    @GetMapping
    public ResponseEntity<List<Estado>> listarTodos() {
        List<Estado> estados = estadoService.obtenerTodosLosEstados();
        return ResponseEntity.ok(estados);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Estado> buscarPorCodigo(@PathVariable Short codigo) {
        return estadoService.obtenerEstadoPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    @GetMapping("/cve/{cve}")
    public ResponseEntity<Estado> buscarPorCve(@PathVariable String cve) {
        return estadoService.obtenerEstadoPorCve(cve)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}