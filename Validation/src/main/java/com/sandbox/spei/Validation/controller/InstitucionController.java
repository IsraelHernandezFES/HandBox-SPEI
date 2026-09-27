package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.entity.Institucion;
import com.sandbox.spei.Validation.service.InstitucionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/instituciones")
public class InstitucionController {

    private final InstitucionService institucionService;

    public InstitucionController(InstitucionService institucionService) {
        this.institucionService = institucionService;
    }

    @GetMapping
    public ResponseEntity<List<Institucion>> listarTodas() {
        return ResponseEntity.ok(institucionService.obtenerTodasLasInstituciones());
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<Institucion> buscarPorCodigo(@PathVariable Integer codigo) {
        return institucionService.obtenerInstitucionPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/estado-operativo/{estado}")
    public ResponseEntity<List<Institucion>> listarPorEstadoOperativo(@PathVariable String estado) {
        return ResponseEntity.ok(institucionService.obtenerPorEstadoOperativo(estado));
    }
}