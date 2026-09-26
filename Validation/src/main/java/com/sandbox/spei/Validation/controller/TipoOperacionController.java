package com.sandbox.spei.Validation.controller;


import com.sandbox.spei.Validation.entity.TipoOperacion;
import com.sandbox.spei.Validation.service.TipoOperacionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipos-operacion")
public class TipoOperacionController {

    private final TipoOperacionService tipoOperacionService;

    public TipoOperacionController(TipoOperacionService tipoOperacionService) {
        this.tipoOperacionService = tipoOperacionService;
    }

    @GetMapping
    public ResponseEntity<List<TipoOperacion>> listarTodos() {
        List<TipoOperacion> tipos = tipoOperacionService.obtenerTodosLosTiposOperacion();
        return ResponseEntity.ok(tipos);
    }

    @GetMapping("/{codigo}")
    public ResponseEntity<TipoOperacion> buscarPorCodigo(@PathVariable String codigo) {
        return tipoOperacionService.obtenerTipoOperacionPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}