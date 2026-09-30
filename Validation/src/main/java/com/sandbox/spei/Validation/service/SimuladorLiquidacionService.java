package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.entity.Estado;
import com.sandbox.spei.Validation.entity.OrdenPago;
import com.sandbox.spei.Validation.repository.EstadoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SimuladorLiquidacionService {

    private final EstadoRepository estadoRepository;

    // Cuentas reservadas por contrato para disparar escenarios
    public static final String CUENTA_FONDOS_INSUFICIENTES = "801000000000000020";
    public static final String CUENTA_INEXISTENTE = "801000000000000021";

    public record ResultadoSimulacion(Estado estadoFinal, String codigoMotivo, String motivo) {}

    public ResultadoSimulacion simular(OrdenPago orden , String escenarioForzado) {
        String cuentaBeneficiaria = orden.getCuentaBeneficiaria();
        Integer institucion = orden.getInstitucion().getCodigo();

        // 1. PRIORIDAD 1: Si viene un escenario forzado en el Header (Sandbox)
        if (escenarioForzado != null && !escenarioForzado.trim().isEmpty()) {
            switch (escenarioForzado.toUpperCase()) {
                case "PRX-020":
                    return new ResultadoSimulacion(obtenerEstado("S04"), "PRX-020", "Fondos insuficientes en el ordenante");
                case "PRX-021":
                    return new ResultadoSimulacion(obtenerEstado("S04"), "PRX-021", "Cuenta receptora inexistente o cancelada");
                case "PRX-022":
                    return new ResultadoSimulacion(obtenerEstado("S04"), "PRX-022", "Institucion receptora no disponible");
                case "PRX-023":
                    return new ResultadoSimulacion(obtenerEstado("S02"), "PRX-023", "Sin respuesta de la institucion receptora");
                case "PRX-024":
                    // Si no tienes S05, puedes usar S02 para que se quede "En Investigación"
                    return new ResultadoSimulacion(obtenerEstado("S02"), "PRX-024", "Operacion marcada para investigacion");
            }
        }
        // S04: Institucion receptora en mantenimiento (805 Praxis Delta) -> PRX-022
        if (institucion != null && institucion == 805) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-022", "Institucion receptora no disponible");
        }

        // Regla A18: Cuenta receptora con dígitos 14 a 17 iguales a 9002 -> PRX-020
        if (cuentaBeneficiaria != null && (cuentaBeneficiaria.contains("9002") || (cuentaBeneficiaria.length() == 18 && "9002".equals(cuentaBeneficiaria.substring(13, 17))))) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-020", "Fondos insuficientes en el ordenante");
        }

        // Regla A19: Cuenta receptora con patrón 9003 -> PRX-021
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.contains("9003")) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-021", "Cuenta receptora inexistente o cancelada");
        }

        // Regla A22: Cuenta receptora con patrón 9004 -> PRX-022 (FALTABA ESTA REGLA)
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.contains("9004")) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-022", "Institucion receptora no disponible");
        }

        // Regla A22: Cuenta receptora con patrón 9004 -> PRX-022
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.contains("9004")) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-022", "Institucion receptora no disponible");
        }

        // Regla A26: Cuenta receptora con patrón 9005 -> Permanece en S02 (EN_PROCESO)
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.contains("9005")) {
            Estado enProceso = obtenerEstado("S02");
            return new ResultadoSimulacion(enProceso, "PRX-026", "Operacion permanece en proceso por regla A26");
        }

        // Regla para patrón 9006 -> Operación en investigación
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.contains("9006")) {
            Estado enInvestigacion = obtenerEstado("EN_INVESTIGACION");
            return new ResultadoSimulacion(enInvestigacion, "PRX-027", "Operacion en investigacion");
        }

        // Liquidacion exitosa (Caso normal) -> S03
        Estado liquidado = obtenerEstado("S03");
        return new ResultadoSimulacion(liquidado, null, "Liquidacion exitosa");
    }

    private Estado obtenerEstado(String cve) {
        return estadoRepository.findByCve(cve)
                .orElseThrow(() -> new IllegalStateException("Estado no configurado en BD: " + cve));
    }
}