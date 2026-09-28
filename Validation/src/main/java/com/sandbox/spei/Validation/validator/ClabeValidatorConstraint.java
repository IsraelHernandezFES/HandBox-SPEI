package com.sandbox.spei.Validation.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ClabeValidatorConstraint implements ConstraintValidator<ValidClabe, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Dejar las validaciones @NotBlank para obligatorios
        }
        // Valida longitud (V01) y dígito verificador (V02)
        return ClabeUtils.esClabeValida(value);
    }
}