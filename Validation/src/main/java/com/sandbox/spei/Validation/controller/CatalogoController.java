package com.sandbox.spei.Validation.controller;

import com.sandbox.spei.Validation.dto.response.ErrorCatalogoDto;
import com.sandbox.spei.Validation.entity.Institucion;
import com.sandbox.spei.Validation.service.InstitucionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/catalogos")
public class CatalogoController {

    private final InstitucionService institucionService;

    public CatalogoController(InstitucionService institucionService) {
        this.institucionService = institucionService;
    }

    // GET /api/v1/catalogos/instituciones - Catálogo ficticio de instituciones (§5.3)
    @GetMapping("/instituciones")
    public ResponseEntity<List<Institucion>> listarInstituciones() {
        return ResponseEntity.ok(institucionService.obtenerTodasLasInstituciones());
    }


    @GetMapping("/instituciones/{codigo}")
    public ResponseEntity<Institucion> buscarPorCodigo(@PathVariable Integer codigo) {
        return institucionService.obtenerInstitucionPorCodigo(codigo)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/instituciones/estado-operativo/{estado}")
    public ResponseEntity<List<Institucion>> listarPorEstadoOperativo(@PathVariable String estado) {
        return ResponseEntity.ok(institucionService.obtenerPorEstadoOperativo(estado));
    }

    // GET /api/v1/catalogos/errores - Catálogo informativo de errores
    @GetMapping("/errores")
    public ResponseEntity<List<ErrorCatalogoDto>> listarErrores() {
        List<ErrorCatalogoDto> errores = List.of(
                new ErrorCatalogoDto("PRX-001", "La cuenta debe tener exactamente 18 digitos numericos"),
                new ErrorCatalogoDto("PRX-002", "Digito verificador de la CLABE incorrecto"),
                new ErrorCatalogoDto("PRX-003", "Institucion inexistente en el catalogo"),
                new ErrorCatalogoDto("PRX-004", "El importe debe ser mayor que cero"),
                new ErrorCatalogoDto("PRX-005", "El importe excede 1,000,000.00 o tiene mas de dos decimales"),
                new ErrorCatalogoDto("PRX-006", "La divisa debe ser MXN"),
                new ErrorCatalogoDto("PRX-007", "El concepto es obligatorio, de 1 a 40 caracteres"),
                new ErrorCatalogoDto("PRX-008", "El folio numerico debe ser un entero entre 1 y 9,999,999"),
                new ErrorCatalogoDto("PRX-009", "La referencia de seguimiento es obligatoria, alfanumerica, de 1 a 30 caracteres"),
                new ErrorCatalogoDto("PRX-013", "La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma"),
                new ErrorCatalogoDto("PRX-015", "Clave-Idempotencia reutilizada con cuerpo distinto"),
                new ErrorCatalogoDto("PRX-030", "Los tres primeros dígitos de la cuenta no coinciden con la institución declarada"),
                new ErrorCatalogoDto("PRX-031", "El monto de la operación debe ser mayor a cero")
        );
        return ResponseEntity.ok(errores);
    }

}