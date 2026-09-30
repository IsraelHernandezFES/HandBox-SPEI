package com.sandbox.spei.Validation.service.impl;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import com.sandbox.spei.Validation.dto.response.TransicionDto;
import com.sandbox.spei.Validation.entity.*;
import com.sandbox.spei.Validation.exception.SpeiException;
import com.sandbox.spei.Validation.repository.*;
import com.sandbox.spei.Validation.service.IdempotenciaService;
import com.sandbox.spei.Validation.service.OrdenPagoService;
import com.sandbox.spei.Validation.service.SimuladorLiquidacionService;
import com.sandbox.spei.Validation.validator.SpeiBusinessValidator;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.sandbox.spei.Validation.entity.Institucion;

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
    // AQUÍ ESTÁ EL CAMBIO: Se agregó String escenarioForzado
    public OperacionResponse procesarOperacionT2T(OperacionT2TRequest request, String claveIdempotencia, String escenarioForzado) {
        Optional<OperacionResponse> cache = idempotenciaService.verificarIdempotencia(claveIdempotencia, request);
        if (cache.isPresent()) {
            OperacionResponse responseCache = cache.get();
            responseCache.setFromCache(true);
            return responseCache;
        }

        if (ordenPagoRepository.existsByClaveRastreo(request.getReferenciaSeguimiento())) {
            throw new SpeiException(
                    "PRX-010",
                    "Referencia de seguimiento registrada previamente",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        speiBusinessValidator.validarT2T(request);

        Integer codigoInst = Integer.parseInt(request.getReceptor().getInstitucion());
        Institucion institucion = institucionRepository.findById(codigoInst)
                .orElseThrow(() -> new SpeiException("PRX-003", "Institucion inexistente en el catalogo", HttpStatus.UNPROCESSABLE_ENTITY));

        // NUEVA REGLA: Bloquear si la institución no está en operación normal
        if (!"NORMAL".equalsIgnoreCase(institucion.getEstadoOperativo())) {
            throw new SpeiException(
                    "PRX-011",
                    "La institución " + institucion.getNombre() + " se encuentra en estado " + institucion.getEstadoOperativo() + " y no puede recibir transferencias",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        TipoOperacion tipoOp = tipoOperacionRepository.findById("T2T")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Tipo de operacion T2T no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        Estado estadoRecibido = estadoRepository.findByCve("S01")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S01 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

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

        Estado estadoEnProceso = estadoRepository.findByCve("S02")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S02 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));
        orden.setEstadoActual(estadoEnProceso);
        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 2, estadoEnProceso, "Operacion enviada a la institucion receptora", orden.getClaveRastreo());

        // Ahora escenarioForzado sí se reconoce porque está en la firma
        var simulacion = simuladorLiquidacionService.simular(orden, escenarioForzado);
        orden.setEstadoActual(simulacion.estadoFinal());
        orden = ordenPagoRepository.save(orden);

        // CORRECCIÓN: Concatenamos el PRX al motivo, pero conservamos intacta la Clave de Rastreo
        String motivoLog = simulacion.codigoMotivo() != null
                ? simulacion.motivo() + " (" + simulacion.codigoMotivo() + ")"
                : simulacion.motivo();

        registrarLog(orden, (short) 3, simulacion.estadoFinal(), motivoLog, orden.getClaveRastreo());
        OperacionResponse response = mapearAResponse(orden);
        if (simulacion.codigoMotivo() != null) {
            response.setCodigoMotivo(simulacion.codigoMotivo());
        }

        idempotenciaService.registrarOperacion(claveIdempotencia, request, response, 201);
        return response;
    }

    @Override
    @Transactional
    // AQUÍ ESTÁ EL CAMBIO: Se agregó String escenarioForzado
    public OperacionResponse procesarOperacionVNT(OperacionVNTRequest request, String claveIdempotencia, String escenarioForzado) {
        Optional<OperacionResponse> cache = idempotenciaService.verificarIdempotencia(claveIdempotencia, request);
        if (cache.isPresent()) {
            OperacionResponse responseCache = cache.get();
            responseCache.setFromCache(true);
            return responseCache;
        }

        if (ordenPagoRepository.existsByClaveRastreo(request.getReferenciaSeguimiento())) {
            throw new SpeiException(
                    "PRX-010",
                    "Referencia de seguimiento registrada previamente",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        speiBusinessValidator.validarVNT(request);

        Integer codigoInst = Integer.parseInt(request.getReceptor().getInstitucion());
        Institucion institucion = institucionRepository.findById(codigoInst)
                .orElseThrow(() -> new SpeiException("PRX-003", "Institucion inexistente en el catalogo", HttpStatus.UNPROCESSABLE_ENTITY));

        // NUEVA REGLA: Bloquear si la institución no está en operación normal
        if (!"NORMAL".equalsIgnoreCase(institucion.getEstadoOperativo())) {
            throw new SpeiException(
                    "PRX-011",
                    "La institución " + institucion.getNombre() + " se encuentra en estado " + institucion.getEstadoOperativo() + " y no puede recibir transferencias",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        TipoOperacion tipoOp = tipoOperacionRepository.findById("VNT")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Tipo de operacion VNT no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        Estado estadoRecibido = estadoRepository.findByCve("S01")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S01 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        OrdenPago orden = OrdenPago.builder()
                .claveRastreo(request.getReferenciaSeguimiento())
                .monto(request.getImporte().getValor())
                .moneda(request.getImporte().getDivisa())
                .nombreOrdenante(request.getEmisor().getNombre())
                .cuentaOrdenante(request.getEmisor().getSucursal())
                .nombreBeneficiario(request.getReceptor().getNombre())
                .cuentaBeneficiaria(request.getReceptor().getCuenta())
                .institucion(institucion)
                .tipoOperacion(tipoOp)
                .estadoActual(estadoRecibido)
                .build();

        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 1, estadoRecibido, "Instruccion ventanilla aceptada", orden.getClaveRastreo());

        Estado estadoEnProceso = estadoRepository.findByCve("S02")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado S02 no configurado", HttpStatus.INTERNAL_SERVER_ERROR));
        orden.setEstadoActual(estadoEnProceso);
        orden = ordenPagoRepository.save(orden);
        registrarLog(orden, (short) 2, estadoEnProceso, "Operacion enviada a la institucion receptora", orden.getClaveRastreo());

        // Ahora escenarioForzado sí se reconoce porque está en la firma
        var simulacion = simuladorLiquidacionService.simular(orden, escenarioForzado);
        orden.setEstadoActual(simulacion.estadoFinal());
        orden = ordenPagoRepository.save(orden);

        // CORRECCIÓN: Concatenamos el PRX al motivo, pero conservamos intacta la Clave de Rastreo
        String motivoLog = simulacion.codigoMotivo() != null
                ? simulacion.motivo() + " (" + simulacion.codigoMotivo() + ")"
                : simulacion.motivo();

        registrarLog(orden, (short) 3, simulacion.estadoFinal(), motivoLog, orden.getClaveRastreo());
        OperacionResponse response = mapearAResponse(orden);
        if (simulacion.codigoMotivo() != null) {
            response.setCodigoMotivo(simulacion.codigoMotivo());
        }

        idempotenciaService.registrarOperacion(claveIdempotencia, request, response, 201);
        return response;
    }

    @Override
    public Optional<OperacionResponse> obtenerPorReferencia(String id) {
        return ordenPagoRepository.findById(Long.parseLong(id)).map(orden -> {
            OperacionResponse response = mapearAResponse(orden);

            // Armar la lista de transiciones tal como lo exige el punto 5.6
            List<TransicionDto> historial = ordenLogRepository.findByOrdenPagoOrderByStepAsc(orden)
                    .stream()
                    .map(log -> TransicionDto.builder()
                            .estado(log.getEstado().getCve()) // O .getNombre() si prefieres el texto
                            .momento(log.getHoraProcesamiento())
                            .motivo(log.getMotivo())
                            .build())
                    .toList();

            response.setTransiciones(historial);
            return response;
        });
    }

    @Override
    public Page<OperacionResponse> obtenerTodasLasOrdenes(Pageable pageable) {
        return ordenPagoRepository.findAll(pageable).map(this::mapearAResponse);
    }

    @Override
    @Transactional
    public OperacionResponse actualizarEstado(Long id, String nuevoEstadoCve) {
        OrdenPago orden = ordenPagoRepository.findById(id)
                .orElseThrow(() -> new SpeiException("PRX-404", "Operación no encontrada", HttpStatus.NOT_FOUND));

        speiBusinessValidator.validarTransicionEstado(orden.getEstadoActual(), nuevoEstadoCve);

        Estado nuevoEstado = estadoRepository.findByCve(nuevoEstadoCve)
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        orden.setEstadoActual(nuevoEstado);
        orden = ordenPagoRepository.save(orden);

        return mapearAResponse(orden);
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