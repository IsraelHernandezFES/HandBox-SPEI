package com.sandbox.spei.Validation.validator;

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
        String cuentaOrdenante = request.getEmisor() != null ? request.getEmisor().getCuenta() : null;
        String cuentaBeneficiaria = request.getReceptor() != null ? request.getReceptor().getCuenta() : null;
        String institucionReceptorStr = request.getReceptor() != null ? request.getReceptor().getInstitucion() : null;

        // Regla PRX-013: La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma
        if (cuentaOrdenante != null && cuentaOrdenante.equals(cuentaBeneficiaria)) {
            throw new SpeiException(
                    "PRX-013",
                    "La cuenta ordenante y la cuenta beneficiaria no pueden ser la misma.",
                    HttpStatus.UNPROCESSABLE_ENTITY
            );
        }

        // Regla PRX-003: Validar que la institución del receptor exista en el catálogo
        if (institucionReceptorStr != null) {
            try {
                Integer codInst = Integer.parseInt(institucionReceptorStr);
                boolean existeInstitucion = institucionRepository.existsById(codInst);
                if (!existeInstitucion) {
                    throw new SpeiException(
                            "PRX-003",
                            "Institución inexistente en el catálogo.",
                            HttpStatus.UNPROCESSABLE_ENTITY
                    );
                }
            } catch (NumberFormatException e) {
                throw new SpeiException(
                        "PRX-003",
                        "Código de institución inválido.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-030 (Ordenante): Los 3 primeros dígitos de la cuenta ordenante deben coincidir con la institución emisora
        if (cuentaOrdenante != null && cuentaOrdenante.length() >= 3 && request.getEmisor().getInstitucion() != null) {
            String prefijoOrdenante = cuentaOrdenante.substring(0, 3);
            String institucionEmisorStr = request.getEmisor().getInstitucion();

            if (!prefijoOrdenante.equals(institucionEmisorStr)) {
                throw new SpeiException(
                        "PRX-030",
                        "Los tres primeros dígitos de la cuenta ordenante no coinciden con la institución declarada.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Validación para el banco beneficiario (verificar que los primeros 3 dígitos coincidan con su institución)
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

        // Regla PRX-031: Validación de montos
        if (request.getImporte() != null && request.getImporte().getValor() != null) {
            if (request.getImporte().getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SpeiException(
                        "PRX-031",
                        "El monto de la operación debe ser mayor a cero.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }
    }

    /**
     * Valida las reglas de negocio específicas para operaciones VNT (Ventanilla a Tercero)
     */
    public void validarVNT(OperacionVNTRequest request) {
        String institucionReceptorStr = request.getReceptor() != null ? request.getReceptor().getInstitucion() : null;
        String cuentaBeneficiaria = request.getReceptor() != null ? request.getReceptor().getCuenta() : null;

        // Regla PRX-003: Validar que la institución exista
        if (institucionReceptorStr != null) {
            try {
                Integer codInst = Integer.parseInt(institucionReceptorStr);
                boolean existeInstitucion = institucionRepository.existsById(codInst);
                if (!existeInstitucion) {
                    throw new SpeiException(
                            "PRX-003",
                            "Institución inexistente en el catálogo.",
                            HttpStatus.UNPROCESSABLE_ENTITY
                    );
                }
            } catch (NumberFormatException e) {
                throw new SpeiException(
                        "PRX-003",
                        "Código de institución inválido.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-030: Validar prefijo de cuenta beneficiaria vs institución
        if (cuentaBeneficiaria != null && cuentaBeneficiaria.length() >= 3 && institucionReceptorStr != null) {
            String prefijoCuenta = cuentaBeneficiaria.substring(0, 3);
            if (!prefijoCuenta.equals(institucionReceptorStr)) {
                throw new SpeiException(
                        "PRX-030",
                        "Los tres primeros dígitos de la cuenta no coinciden con la institución declarada.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }

        // Regla PRX-031: Validación de montos
        if (request.getImporte() != null && request.getImporte().getValor() != null) {
            if (request.getImporte().getValor().compareTo(BigDecimal.ZERO) <= 0) {
                throw new SpeiException(
                        "PRX-031",
                        "El monto de la operación debe ser mayor a cero.",
                        HttpStatus.UNPROCESSABLE_ENTITY
                );
            }
        }
    }
}