package com.sandbox.spei.Validation.service.impl;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import com.sandbox.spei.Validation.entity.*;
import com.sandbox.spei.Validation.exception.SpeiException;
import com.sandbox.spei.Validation.repository.*;
import com.sandbox.spei.Validation.service.IdempotenciaService;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import com.sandbox.spei.Validation.service.SimuladorLiquidacionService;
import com.sandbox.spei.Validation.validator.SpeiBusinessValidator;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class OrdenPagoServiceImpl implements OrdenPagoService {

    private final OrdenPagoRepository ordenPagoRepository;
    private final OrdenLogRepository ordenLogRepository;
    private final IdempotenciaService idempotenciaService;
    private final SpeiBusinessValidator speiBusinessValidator;
    private final SimuladorLiquidacionService simuladorLiquidacionService;
    private final InstitucionRepository institucionRepository;
    private final TipoOperacionRepository tipoOperacionRepository;
    private final EstadoRepository estadoRepository;

    public OrdenPagoServiceImpl(OrdenPagoRepository ordenPagoRepository,
                                OrdenLogRepository ordenLogRepository,
                                IdempotenciaService idempotenciaService,
                                SpeiBusinessValidator speiBusinessValidator,
                                SimuladorLiquidacionService simuladorLiquidacionService,
                                InstitucionRepository institucionRepository,
                                TipoOperacionRepository tipoOperacionRepository,
                                EstadoRepository estadoRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.ordenLogRepository = ordenLogRepository;
        this.idempotenciaService = idempotenciaService;
        this.speiBusinessValidator = speiBusinessValidator;
        this.simuladorLiquidacionService = simuladorLiquidacionService;
        this.institucionRepository = institucionRepository;
        this.tipoOperacionRepository = tipoOperacionRepository;
        this.estadoRepository = estadoRepository;
    }

    @Override
    @Transactional
    public OperacionResponse procesarOperacionT2T(OperacionT2TRequest request, String claveIdempotencia) {
        // 1. Validar Idempotencia (si viene encabezado)
        Optional<OperacionResponse> cache = idempotenciaService.verificarIdempotencia(claveIdempotencia, request);
        if (cache.isPresent()) {
            return cache.get();
        }

        // 2. Validar Reglas de Negocio cruzadas
        speiBusinessValidator.validarT2T(request);

        // 3. Buscar catálogos (la institución viene dentro del emisor o receptor según el contrato)
        Integer codigoInst = Integer.parseInt(request.getReceptor().getInstitucion());
        Institucion institucion = institucionRepository.findById(codigoInst)
                .orElseThrow(() -> new SpeiException("PRX-003", "Institucion inexistente en el catalogo", HttpStatus.UNPROCESSABLE_ENTITY));

        TipoOperacion tipoOp = tipoOperacionRepository.findById("T2T")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Tipo de operacion T2T no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        Estado estadoRecibido = estadoRepository.findByCve("S01")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S01 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        // 4. Paso 1: Guardar en estado S01 (Recibido) usando los objetos anidados
        OrdenPago orden = OrdenPago.builder()
                .claveRastreo(request.getReferenciaSeguimiento())
                .monto(request.getImporte().getValor())
                .moneda(request.getImporte().getDivisa())
                .nombreOrdenante(request.getEmisor().getNombre())
                .cuentaOrdenante(request.getEmisor().getCuenta())
                .nombreBeneficiario(request.getReceptor().getNombre())
                .cuentaBeneficiaria(request.getReceptor().getCuenta())
                .institucion(institucion)
                .tipoOperacion(tipoOp)
                .estadoActual(estadoRecibido)
                .build();
        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 1, estadoRecibido, "Instruccion aceptada y persistida", orden.getClaveRastreo());

        // 5. Paso 2: Transición a S02 (En proceso)
        Estado estadoEnProceso = estadoRepository.findByCve("S02")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S02 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));
        orden.setEstadoActual(estadoEnProceso);
        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 2, estadoEnProceso, "Operacion enviada a la institucion receptora", orden.getClaveRastreo());

        // 6. Paso 3: Simulación de liquidación
        var simulacion = simuladorLiquidacionService.simular(orden);
        orden.setEstadoActual(simulacion.estadoFinal());
        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 3, simulacion.estadoFinal(), simulacion.motivo(),
                simulacion.codigoMotivo() != null ? simulacion.codigoMotivo() : orden.getClaveRastreo());

        // 7. Mapear respuesta y registrar en idempotencia
        OperacionResponse response = mapearAResponse(orden);
        idempotenciaService.registrarOperacion(claveIdempotencia, request, response, 201);

        return response;
    }

    @Override
    @Transactional
    public OperacionResponse procesarOperacionVNT(OperacionVNTRequest request, String claveIdempotencia) {
        Optional<OperacionResponse> cache = idempotenciaService.verificarIdempotencia(claveIdempotencia, request);
        if (cache.isPresent()) {
            return cache.get();
        }

        speiBusinessValidator.validarVNT(request);

        // La institución se obtiene del receptor o emisor según el esquema anidado
        Integer codigoInst = Integer.parseInt(request.getReceptor().getInstitucion());
        Institucion institucion = institucionRepository.findById(codigoInst)
                .orElseThrow(() -> new SpeiException("PRX-003", "Institucion inexistente en el catalogo", HttpStatus.UNPROCESSABLE_ENTITY));

        TipoOperacion tipoOp = tipoOperacionRepository.findById("VNT")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Tipo de operacion VNT no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        // Buscar el estado oficial S01 del contrato
        Estado estadoRecibido = estadoRepository.findByCve("S01")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S01 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        // Mapeo utilizando los objetos anidados (importe, emisor con sucursal, receptor)
        OrdenPago orden = OrdenPago.builder()
                .claveRastreo(request.getReferenciaSeguimiento())
                .monto(request.getImporte().getValor())
                .moneda(request.getImporte().getDivisa())
                .nombreOrdenante(request.getEmisor().getNombre())
                .cuentaOrdenante(request.getEmisor().getSucursal()) // Sucursal para VNT
                .nombreBeneficiario(request.getReceptor().getNombre())
                .cuentaBeneficiaria(request.getReceptor().getCuenta())
                .institucion(institucion)
                .tipoOperacion(tipoOp)
                .estadoActual(estadoRecibido)
                .build();

        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 1, estadoRecibido, "Instruccion ventanilla aceptada", orden.getClaveRastreo());

        // Buscar el estado oficial S02 del contrato
        Estado estadoEnProceso = estadoRepository.findByCve("S02")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S02 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));
        orden.setEstadoActual(estadoEnProceso);
        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 2, estadoEnProceso, "Operacion enviada a la institucion receptora", orden.getClaveRastreo());

        var simulacion = simuladorLiquidacionService.simular(orden);
        orden.setEstadoActual(simulacion.estadoFinal());
        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 3, simulacion.estadoFinal(), simulacion.motivo(),
                simulacion.codigoMotivo() != null ? simulacion.codigoMotivo() : orden.getClaveRastreo());

        OperacionResponse response = mapearAResponse(orden);
        idempotenciaService.registrarOperacion(claveIdempotencia, request, response, 201);

        return response;
    }
    @Override
    public Optional<OperacionResponse> obtenerPorReferencia(String id) {
        // Si tu ID es Long, conviértelo: Long.parseLong(id)
        return ordenPagoRepository.findById(Long.parseLong(id)).map(this::mapearAResponse);
    }

    @Override
    public List<OperacionResponse> obtenerTodasLasOrdenes() {
        return ordenPagoRepository.findAll().stream().map(this::mapearAResponse).toList();
    }

    private void registrarLog(OrdenPago orden, short step, Estado estado, String motivo, String cveRastreo) {
        OrdenLog log = OrdenLog.builder()
                .ordenPago(orden)
                .step(step)
                .estado(estado)
                .motivo(motivo)
                .cveRastreo(cveRastreo)
                .build();
        ordenLogRepository.save(log);
    }

    private OperacionResponse mapearAResponse(OrdenPago o) {
        return OperacionResponse.builder()
                .id(o.getId())
                .claveRastreo(o.getClaveRastreo())
                .tipoOperacion(o.getTipoOperacion().getCodigo())
                .monto(o.getMonto())
                .moneda(o.getMoneda())
                .estadoActual(o.getEstadoActual().getCve())
                .nombreOrdenante(o.getNombreOrdenante())
                .cuentaOrdenante(o.getCuentaOrdenante())
                .nombreBeneficiario(o.getNombreBeneficiario())
                .cuentaBeneficiaria(o.getCuentaBeneficiaria())
                .codigoInstitucion(o.getInstitucion().getCodigo())
                .fechaCreacion(o.getFecha())
                .build();
    }
}