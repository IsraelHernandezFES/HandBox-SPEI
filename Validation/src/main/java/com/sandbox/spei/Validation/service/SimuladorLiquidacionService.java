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

    public ResultadoSimulacion simular(OrdenPago orden) {
        String cuentaBeneficiaria = orden.getCuentaBeneficiaria();
        Integer institucion = orden.getInstitucion().getCodigo();

        // S04: Institucion receptora en mantenimiento (805 Praxis Delta) -> PRX-022
        if (institucion != null && institucion == 805) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-022", "Institucion receptora no disponible");
        }

        // Regla A18: Cuenta receptora con dígitos 14 a 17 iguales a 9002 -> PRX-020
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.length() == 18) {
            // substring(13, 17) extrae los caracteres en los índices 13, 14, 15 y 16 (que son los dígitos 14, 15, 16 y 17)
            String digitos14a17 = cuentaBeneficiaria.substring(13, 17);
            if ("9002".equals(digitos14a17)) {
                Estado devuelto = obtenerEstado("S04");
                return new ResultadoSimulacion(devuelto, "PRX-020", "Fondos insuficientes en el ordenante");
            }
        }

        // Regla A19: Cuenta receptora con patrón 9003 -> PRX-021
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.contains("9003")) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-021", "Cuenta receptora inexistente o cancelada");
        }

        // Cuenta exacta inexistente o fondos insuficientes (compatibilidad previa)
        if (CUENTA_FONDOS_INSUFICIENTES.equals(cuentaBeneficiaria)) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-020", "Fondos insuficientes en el ordenante");
        }
        if (CUENTA_INEXISTENTE.equals(cuentaBeneficiaria)) {
            Estado devuelto = obtenerEstado("S04");
            return new ResultadoSimulacion(devuelto, "PRX-021", "Cuenta receptora inexistente o cancelada");
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