package com.sandbox.spei.Validation.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = ClabeValidatorConstraint.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidClabe {
    String message() default "PRX-002: Digito verificador de la CLABE incorrecto";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}