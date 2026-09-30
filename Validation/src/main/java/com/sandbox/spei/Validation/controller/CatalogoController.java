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
                new ErrorCatalogoDto("PRX-004", "El importe debe ser estrictamente mayor que cero"), // V06
                new ErrorCatalogoDto("PRX-005", "El importe excede 1,000,000.00 o tiene mas de dos decimales"),
                new ErrorCatalogoDto("PRX-006", "La divisa debe ser MXN"),
                new ErrorCatalogoDto("PRX-007", "El concepto es obligatorio, entre uno y cuarenta caracteres"),
                new ErrorCatalogoDto("PRX-008", "El folio numerico debe ser un entero entre 1 y 9,999,999"),
                new ErrorCatalogoDto("PRX-009", "La referencia de seguimiento es obligatoria, alfanumerica, de 1 a 30 caracteres"),
                new ErrorCatalogoDto("PRX-010", "Referencia de seguimiento registrada previamente"), // V12 / A16
                new ErrorCatalogoDto("PRX-011", "Campo obligatorio faltante (cuenta, sucursal, documentoIdentidad o nombre)"), // V14, V16, V18
                new ErrorCatalogoDto("PRX-012", "Campo prohibido presente para este tipo de operacion"), // V15, V17
                new ErrorCatalogoDto("PRX-013", "La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma"),
                new ErrorCatalogoDto("PRX-014", "Transicion de estado prohibida: de LIQUIDADO a DEVUELTO"),
                new ErrorCatalogoDto("PRX-015", "Clave-Idempotencia reutilizada con cuerpo distinto"),
                new ErrorCatalogoDto("PRX-020", "Fondos insuficientes en el ordenante"),
                new ErrorCatalogoDto("PRX-021", "Cuenta receptora inexistente o cancelada"),
                new ErrorCatalogoDto("PRX-022", "Institucion receptora no disponible"),
                new ErrorCatalogoDto("PRX-023", "Sin respuesta de la institución receptora"),
                new ErrorCatalogoDto("PRX-024", "Operación marcada para investigación"),
                new ErrorCatalogoDto("PRX-030", "Los tres primeros digitos de la cuenta no coinciden con la institucion declarada"),
                new ErrorCatalogoDto("PRX-031", "El tipo de operacion debe ser estrictamente T2T o VNT") // V13 ajustado a la tabla
        );
        return ResponseEntity.ok(errores);
    }

}