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
        List<Institucion> instituciones = institucionService.obtenerTodasLasInstituciones();
        return ResponseEntity.ok(instituciones);
    }


    @GetMapping("/{codigo}")
    public ResponseEntity<Institucion> buscarPorCodigo(@PathVariable Integer codigo) {
        return institucionService.obtenerInstitucionPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }


    @GetMapping("/estado/{estadoOperativo}")
    public ResponseEntity<List<Institucion>> listarPorEstadoOperativo(@PathVariable String estadoOperativo) {
        List<Institucion> instituciones = institucionService.obtenerPorEstadoOperativo(estadoOperativo);
        if (instituciones.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(instituciones);
    }

    @GetMapping("/{codigo}/puede-enviar")
    public ResponseEntity<Boolean> validarPuedeEnviar(@PathVariable Integer codigo) {
        boolean puede = institucionService.puedeEnviarPagos(codigo);
        return ResponseEntity.ok(puede);
    }

    @GetMapping("/{codigo}/puede-recibir")
    public ResponseEntity<Boolean> validarPuedeRecibir(@PathVariable Integer codigo) {
        boolean puede = institucionService.puedeRecibirPagos(codigo);
        return ResponseEntity.ok(puede);
    }
}