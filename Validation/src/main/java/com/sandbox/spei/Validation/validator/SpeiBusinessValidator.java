package com.sandbox.spei.Validation.validator;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
import com.sandbox.spei.Validation.entity.Estado;
import com.sandbox.spei.Validation.exception.SpeiException;
import com.sandbox.spei.Validation.repository.InstitucionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class SpeiBusinessValidator {

    private final InstitucionRepository institucionRepository;

    public SpeiBusinessValidator(InstitucionRepository institucionRepository) {
        this.institucionRepository = institucionRepository;
    }

    /**
     * Valida las reglas de negocio específicas para operaciones T2T (Cuenta a Cuenta)
     */
    public void validarT2T(OperacionT2TRequest request) {
        String cuentaOrdenante = request.getEmisor() != null ? request.getEmisor().getCuenta() : null;
        String cuentaBeneficiaria = request.getReceptor() != null ? request.getReceptor().getCuenta() : null;
        String institucionEmisorStr = request.getEmisor() != null ? request.getEmisor().getInstitucion() : null;
        String institucionReceptorStr = request.getReceptor() != null ? request.getReceptor().getInstitucion() : null;

        // --- Regla V04 (PRX-003): Validar que AMBAS instituciones existan en el catálogo ---
        validarExistenciaInstitucion(institucionEmisorStr);
        validarExistenciaInstitucion(institucionReceptorStr);

        // Regla PRX-013: La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma
        if (cuentaOrdenante != null && cuentaOrdenante.equals(cuentaBeneficiaria)) {
            throw new SpeiException(
                    "PRX-013",
                    "La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        // Regla PRX-030 (Ordenante): Los 3 primeros dígitos de la cuenta deben coincidir con la institución
        if (cuentaOrdenante != null && cuentaOrdenante.length() >= 3 && institucionEmisorStr != null) {
            String prefijoOrdenante = cuentaOrdenante.substring(0, 3);
            if (!prefijoOrdenante.equals(institucionEmisorStr)) {
                throw new SpeiException(
                        "PRX-030",
                        "Los tres primeros dígitos de la cuenta ordenante no coinciden con la institución declarada.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-030 (Beneficiario)
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.length() >= 3 && institucionReceptorStr != null) {
            String prefijoBeneficiario = cuentaBeneficiaria.substring(0, 3);
            if (!prefijoBeneficiario.equals(institucionReceptorStr)) {
                throw new SpeiException(
                        "PRX-030",
                        "Los tres primeros dígitos de la cuenta beneficiaria no coinciden con la institución declarada.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-004: Validación de montos > 0
        if (request.getImporte() != null && request.getImporte().getValor() != null) {
            if (request.getImporte().getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SpeiException(
                        "PRX-004",
                        "El monto de la operación debe ser estrictamente mayor que cero.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }
    }

    /**
     * Valida las reglas de negocio específicas para operaciones VNT (Ventanilla a Tercero)
     */
    public void validarVNT(OperacionVNTRequest request) {
        String institucionEmisorStr = request.getEmisor() != null ? request.getEmisor().getInstitucion() : null;
        String institucionReceptorStr = request.getReceptor() != null ? request.getReceptor().getInstitucion() : null;
        String cuentaBeneficiaria = request.getReceptor() != null ? request.getReceptor().getCuenta() : null;

        // --- Regla V04 (PRX-003): Validar que AMBAS instituciones existan en el catálogo ---
        validarExistenciaInstitucion(institucionEmisorStr);
        validarExistenciaInstitucion(institucionReceptorStr);

        // Regla PRX-030: Validar prefijo de cuenta beneficiaria vs institución
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.length() >= 3 && institucionReceptorStr != null) {
            String prefijoCuenta = cuentaBeneficiaria.substring(0, 3);
            if (!prefijoCuenta.equals(institucionReceptorStr)) {
                throw new SpeiException(
                        "PRX-030",
                        "Los tres primeros dígitos de la cuenta beneficiaria no coinciden con la institución declarada.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-004: Validación de montos > 0
        if (request.getImporte() != null && request.getImporte().getValor() != null) {
            if (request.getImporte().getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SpeiException(
                        "PRX-004",
                        "El monto de la operación debe ser estrictamente mayor que cero.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }
    }

    public void validarTransicionEstado(Estado estadoActual, String cveNuevoEstado) {
        if (estadoActual != null && "S03".equals(estadoActual.getCve()) && "S04".equals(cveNuevoEstado)) {
            throw new SpeiException(
                    "PRX-014",
                    "Transición de estado prohibida: no se puede pasar de LIQUIDADO a DEVUELTO",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }

    /**
     * Método auxiliar privado para reutilizar la validación en la base de datos (Regla V04)
     */
    private void validarExistenciaInstitucion(String institucionStr) {
        // Si viene nulo, el DTO y @NotNull ya se encargarán de lanzar PRX-011.
        // Aquí solo validamos si existe en la base de datos cuando SÍ nos envían un valor.
        if (institucionStr == null || institucionStr.trim().isEmpty()) {
            return;
        }

        try {
            Integer codInst = Integer.parseInt(institucionStr);
            boolean existeInstitucion = institucionRepository.existsById(codInst);
            if (!existeInstitucion) {
                throw new SpeiException(
                        "PRX-003",
                        "La institución '" + institucionStr + "' es inexistente en el catálogo.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        } catch (NumberFormatException e) {
            throw new SpeiException(
                    "PRX-003",
                    "El código de institución '" + institucionStr + "' es inválido (no numérico).",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }
}