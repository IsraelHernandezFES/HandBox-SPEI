package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.entity.Estado;
import com.sandbox.spei.Validation.entity.Institucion;
import com.sandbox.spei.Validation.entity.OrdenPago;
import com.sandbox.spei.Validation.entity.TipoOperacion;
import com.sandbox.spei.Validation.exception.SpeiException;
import com.sandbox.spei.Validation.repository.EstadoRepository;
import com.sandbox.spei.Validation.repository.InstitucionRepository;
import com.sandbox.spei.Validation.repository.OrdenPagoRepository;
import com.sandbox.spei.Validation.repository.TipoOperacionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrdenPagoServiceImpl {

    private final OrdenPagoRepository ordenPagoRepository;
    private final IdempotenciaService idempotenciaService;
    private final SpeiBusinessValidator speiBusinessValidator;
    private final SimuladorLiquidacionService simuladorLiquidacionService;
    private final InstitucionRepository institucionRepository;
    private final TipoOperacionRepository tipoOperacionRepository;
    private final EstadoRepository estadoRepository;

    public OrdenPagoServiceImpl(OrdenPagoRepository ordenPagoRepository,
                                IdempotenciaService idempotenciaService,
                                SpeiBusinessValidator speiBusinessValidator,
                                SimuladorLiquidacionService simuladorLiquidacionService,
                                InstitucionRepository institucionRepository,
                                TipoOperacionRepository tipoOperacionRepository,
                                EstadoRepository estadoRepository) {
        this.ordenPagoRepository = ordenPagoRepository;
        this.idempotenciaService = idempotenciaService;
        this.speiBusinessValidator = speiBusinessValidator;
        this.simuladorLiquidacionService = simuladorLiquidacionService;
        this.institucionRepository = institucionRepository;
        this.tipoOperacionRepository = tipoOperacionRepository;
        this.estadoRepository = estadoRepository;
    }

    @Transactional
    public OrdenPago procesarT2T(OperacionT2TRequest request, String claveIdempotencia) {
        // 1. Validar Idempotencia
        String claveRastreo = request.getReferenciaSeguimiento();
        idempotenciaService.validarIdempotencia(claveIdempotencia != null ? claveIdempotencia : claveRastreo);

        // 2. Validar Reglas de Negocio
        speiBusinessValidator.validarT2T(request);

        // 3. Simulación de escenario
        SimuladorLiquidacionService.ResultadoSimulacion simulacion =
                simuladorLiquidacionService.determinarEscenario(request.getCuentaBeneficiaria());

        // 4. Buscar entidades relacionadas
        Institucion institucion = institucionRepository.findById(request.getCodigoInstitucion().longValue())
                .orElseThrow(() -> new SpeiException("PRX-003", "Institución no encontrada", HttpStatus.UNPROCESSABLE_ENTITY));

        TipoOperacion tipoOp = tipoOperacionRepository.findById("T2T")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Tipo de operación T2T no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        Estado estadoInicial = estadoRepository.findByCve("RECIBIDO")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado inicial RECIBIDO no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        // 5. Construir y guardar la orden en estado inicial
        OrdenPago orden = OrdenPago.builder()
                .claveRastreo(claveRastreo)
                .monto(request.getMonto())
                .moneda(request.getDivisa())
                .nombreOrdenante(request.getNombreOrdenante())
                .cuentaOrdenante(request.getCuentaOrdenante())
                .nombreBeneficiario(request.getNombreBeneficiario())
                .cuentaBeneficiaria(request.getCuentaBeneficiaria())
                .institucion(institucion)
                .tipoOperacion(tipoOp)
                .estadoActual(estadoInicial)
                .build();

        orden = ordenPagoRepository.save(orden);

        // 6. Aplicar transición del simulador usando findByCve
        Estado estadoFinal = estadoRepository.findByCve(simulacion.getEstadoFinal())
                .orElse(estadoInicial);
        orden.setEstadoActual(estadoFinal);

        return ordenPagoRepository.save(orden);
    }

    @Transactional
    public OrdenPago procesarVNT(OperacionVNTRequest request, String claveIdempotencia) {
        String claveRastreo = request.getReferenciaSeguimiento();
        idempotenciaService.validarIdempotencia(claveIdempotencia != null ? claveIdempotencia : claveRastreo);

        speiBusinessValidator.validarVNT(request);

        SimuladorLiquidacionService.ResultadoSimulacion simulacion =
                simuladorLiquidacionService.determinarEscenario(request.getCuentaBeneficiaria());

        Institucion institucion = institucionRepository.findById(request.getCodigoInstitucion().longValue())
                .orElseThrow(() -> new SpeiException("PRX-003", "Institución no encontrada", HttpStatus.UNPROCESSABLE_ENTITY));

        TipoOperacion tipoOp = tipoOperacionRepository.findById("VNT")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Tipo de operación VNT no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        Estado estadoInicial = estadoRepository.findByCve("RECIBIDO")
                .orElseThrow(() -> new SpeiException("ERR_DB", "Estado inicial RECIBIDO no configurado", HttpStatus.INTERNAL_SERVER_ERROR));

        OrdenPago orden = OrdenPago.builder()
                .claveRastreo(claveRastreo)
                .monto(request.getMonto())
                .moneda(request.getDivisa())
                .nombreOrdenante(request.getNombreOrdenante())
                .cuentaOrdenante(request.getSucursal())
                .nombreBeneficiario(request.getNombreBeneficiario())
                .cuentaBeneficiaria(request.getCuentaBeneficiaria())
                .institucion(institucion)
                .tipoOperacion(tipoOp)
                .estadoActual(estadoInicial)
                .build();

        orden = ordenPagoRepository.save(orden);

        Estado estadoFinal = estadoRepository.findByCve(simulacion.getEstadoFinal())
                .orElse(estadoInicial);
        orden.setEstadoActual(estadoFinal);

        return ordenPagoRepository.save(orden);
    }
}