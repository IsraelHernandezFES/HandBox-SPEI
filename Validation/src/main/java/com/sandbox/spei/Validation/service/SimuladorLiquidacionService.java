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
            Estado devuelto = obtenerEstado("DEVUELTO");
            return new ResultadoSimulacion(devuelto, "PRX-022", "Institucion receptora no disponible");
        }

        // S02: Fondos insuficientes en el ordenante -> PRX-020
        if (CUENTA_FONDOS_INSUFICIENTES.equals(cuentaBeneficiaria)) {
            Estado devuelto = obtenerEstado("DEVUELTO");
            return new ResultadoSimulacion(devuelto, "PRX-020", "Fondos insuficientes en el ordenante");
        }

        // S03: Cuenta receptora inexistente o cancelada -> PRX-021
        if (CUENTA_INEXISTENTE.equals(cuentaBeneficiaria)) {
            Estado devuelto = obtenerEstado("DEVUELTO");
            return new ResultadoSimulacion(devuelto, "PRX-021", "Cuenta receptora inexistente o cancelada");
        }

        // S01: Liquidacion exitosa (Caso normal)
        Estado liquidado = obtenerEstado("LIQUIDADO");
        return new ResultadoSimulacion(liquidado, null, "Liquidacion exitosa");
    }

    private Estado obtenerEstado(String cve) {
        return estadoRepository.findByCve(cve)
                .orElseThrow(() -> new IllegalStateException("Estado no configurado en BD: " + cve));
    }
}