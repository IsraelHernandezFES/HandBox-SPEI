package com.sandbox.spei.Validation.service;

import com.sandbox.spei.Validation.dto.request.OperacionT2TRequest;
import com.sandbox.spei.Validation.dto.request.OperacionVNTRequest;
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
        // Regla PRX-013: La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma
        if (request.getCuentaOrdenante() != null && request.getCuentaOrdenante().equals(request.getCuentaBeneficiaria())) {
            throw new SpeiException(
                    "PRX-013",
                    "La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        // Regla PRX-003: Validar que la institución emisor exista en el catálogo
        if (request.getCodigoInstitucion() != null) {
            boolean existeInstitucion = institucionRepository.existsById(request.getCodigoInstitucion());
            if (!existeInstitucion) {
                throw new SpeiException(
                        "PRX-003",
                        "Institución inexistente en el catálogo.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-030 (Ordenante): Los 3 primeros dígitos de la cuenta ordenante deben coincidir con la institución emisora
        if (request.getCuentaOrdenante() != null && request.getCuentaOrdenante().length() >= 3 && request.getCodigoInstitucion() != null) {
            String prefijoOrdenante = request.getCuentaOrdenante().substring(0, 3);
            String institucionStr = String.format("%03d", request.getCodigoInstitucion());

            if (!prefijoOrdenante.equals(institucionStr)) {
                throw new SpeiException(
                        "PRX-030",
                        "Los tres primeros dígitos de la cuenta ordenante no coinciden con la institución declarada.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Validación para el banco beneficiario (verificar que exista en el catálogo según sus primeros 3 dígitos)
        if (request.getCuentaBeneficiaria() != null && request.getCuentaBeneficiaria().length() >= 3) {
            String prefijoBeneficiario = request.getCuentaBeneficiaria().substring(0, 3);
            try {
                Integer codBeneficiario = Integer.parseInt(prefijoBeneficiario);
                if (!institucionRepository.existsById(codBeneficiario)) {
                    throw new SpeiException(
                            "PRX-003",
                            "La institución de la cuenta beneficiaria no existe en el catálogo.",
                            HttpStatus.UNPROCESSABLE_ENTITY
                    );
                }
            } catch (NumberFormatException e) {
                throw new SpeiException(
                        "PRX-001",
                        "El prefijo de la cuenta beneficiaria no es válido.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-031: Validación de montos
        if (request.getMonto() != null && request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SpeiException(
                    "PRX-031",
                    "El monto de la operación debe ser mayor a cero.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }

    /**
     * Valida las reglas de negocio específicas para operaciones VNT (Ventanilla a Tercero)
     */
    public void validarVNT(OperacionVNTRequest request) {
        // Regla PRX-003: Validar que la institución exista
        if (request.getCodigoInstitucion() != null) {
            boolean existeInstitucion = institucionRepository.existsById(request.getCodigoInstitucion());
            if (!existeInstitucion) {
                throw new SpeiException(
                        "PRX-003",
                        "Institución inexistente en el catálogo.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-030: Validar prefijo de cuenta beneficiaria vs institución
        if (request.getCuentaBeneficiaria() != null && request.getCuentaBeneficiaria().length() >= 3 && request.getCodigoInstitucion() != null) {
            String prefijoCuenta = request.getCuentaBeneficiaria().substring(0, 3);
            String institucionStr = String.format("%03d", request.getCodigoInstitucion());

            if (!prefijoCuenta.equals(institucionStr)) {
                throw new SpeiException(
                        "PRX-030",
                        "Los tres primeros dígitos de la cuenta no coinciden con la institución declarada.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-031: Validación de montos
        if (request.getMonto() != null && request.getMonto().compareTo(BigDecimal.ZERO) <= 0) {
            throw new SpeiException(
                    "PRX-031",
                    "El monto de la operación debe ser mayor a cero.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }
    }
}