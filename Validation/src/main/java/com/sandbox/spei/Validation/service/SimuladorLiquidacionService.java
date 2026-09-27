package com.sandbox.spei.Validation.service;

import org.springframework.stereotype.Service;

@Service
public class SimuladorLiquidacionService {


     //Determina el escenario y estado final basado en los dígitos 14 a 17 de la cuenta receptora.
     // Si no coincide con ninguna cuenta reservada, por defecto la liquidación es exitosa.

    public ResultadoSimulacion determinarEscenario(String cuentaReceptora) {
        if (cuentaReceptora == null || cuentaReceptora.length() < 17) {
            return new ResultadoSimulacion("LIQUIDADO", null);
        }

        // Extraer los dígitos 14 a 17 (índice 13 al 17)
        String subFijos = cuentaReceptora.substring(13, 17);

        switch (subFijos) {
            case "9002":
                return new ResultadoSimulacion("DEVUELTO", "PRX-020"); // Fondos insuficientes
            case "9003":
                return new ResultadoSimulacion("DEVUELTO", "PRX-021"); // Cuenta receptora inexistente
            case "9004":
                return new ResultadoSimulacion("DEVUELTO", "PRX-022"); // Institución receptora no disponible
            case "9005":
                return new ResultadoSimulacion("EN_PROCESO", "PRX-023"); // Sin respuesta (opcional)
            case "9006":
                return new ResultadoSimulacion("EN_INVESTIGACION", "PRX-024"); // En investigación (opcional)
            default:
                return new ResultadoSimulacion("LIQUIDADO", null); // Comportamiento por omisión (éxito)
        }
    }

    // Clase auxiliar interna para transportar el resultado de la simulación
    public static class ResultadoSimulacion {
        private final String estadoFinal;
        private final String codigoMotivo;

        public ResultadoSimulacion(String estadoFinal, String codigoMotivo) {
            this.estadoFinal = estadoFinal;
            this.codigoMotivo = codigoMotivo;
        }

        public String getEstadoFinal() {
            return estadoFinal;
        }

        public String getCodigoMotivo() {
            return codigoMotivo;
        }
    }
}