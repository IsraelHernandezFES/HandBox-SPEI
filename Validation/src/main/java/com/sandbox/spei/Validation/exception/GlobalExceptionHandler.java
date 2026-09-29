package com.sandbox.spei.Validation.exception;

import com.sandbox.spei.Validation.dto.response.ErrorResponse;
import com.sandbox.spei.Validation.dto.response.ItemErrorResponse;
import com.sandbox.spei.Validation.dto.response.SpeiValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Maneja errores controlados del dominio SPEI (PRX-xxx)
    @ExceptionHandler(SpeiException.class)
    public ResponseEntity<ErrorResponse> handleSpeiException(SpeiException ex) {
        ErrorResponse error = ErrorResponse.builder()
                .codigo(ex.getCodigo())
                .mensaje(ex.getMessage())
                .detalles(ex.getDetalles())
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(ex.getHttpStatus()).body(error);
    }

    // Maneja validaciones fallidas del cuerpo de la petición (@RequestBody con @Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SpeiValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ItemErrorResponse> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> {
                    try {
                        String defaultMsg = err.getDefaultMessage() != null ? err.getDefaultMessage() : "Datos inválidos";
                        String codigo = "PRX-001";
                        String mensaje = defaultMsg;
                        String campo = err.getField() != null ? err.getField() : "desconocido";

                        if (defaultMsg.contains(":")) {
                            String[] parts = defaultMsg.split(":", 2);
                            if (parts.length > 1) {
                                codigo = parts[0].trim();
                                mensaje = parts[1].trim();
                            }
                        }

                        return ItemErrorResponse.builder()
                                .codigo(codigo)
                                .campo(campo)
                                .mensaje(mensaje)
                                .build();
                    } catch (Exception e) {
                        return ItemErrorResponse.builder()
                                .codigo("PRX-001")
                                .campo(err.getField() != null ? err.getField() : "desconocido")
                                .mensaje("Error de validación")
                                .build();
                    }
                })
                .toList();

        String referencia = getReferenciaSeguimiento(request);

        SpeiValidationErrorResponse response = SpeiValidationErrorResponse.builder()
                .referenciaSeguimiento(referencia)
                .errores(errores)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // Maneja violaciones de restricciones en parámetros (ConstraintViolationException)
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<SpeiValidationErrorResponse> handleConstraintViolationException(ConstraintViolationException ex, HttpServletRequest request) {
        List<ItemErrorResponse> errores = ex.getConstraintViolations().stream()
                .map(violation -> {
                    try {
                        String defaultMsg = violation.getMessage() != null ? violation.getMessage() : "Datos inválidos";
                        String codigo = "PRX-001";
                        String mensaje = defaultMsg;

                        String propertyPath = violation.getPropertyPath() != null ? violation.getPropertyPath().toString() : "desconocido";
                        String campo = propertyPath.contains(".") ? propertyPath.substring(propertyPath.lastIndexOf('.') + 1) : propertyPath;

                        if (defaultMsg.contains(":")) {
                            String[] parts = defaultMsg.split(":", 2);
                            if (parts.length > 1) {
                                codigo = parts[0].trim();
                                mensaje = parts[1].trim();
                            }
                        }

                        return ItemErrorResponse.builder()
                                .codigo(codigo)
                                .campo(campo)
                                .mensaje(mensaje)
                                .build();
                    } catch (Exception e) {
                        return ItemErrorResponse.builder()
                                .codigo("PRX-001")
                                .campo("desconocido")
                                .mensaje("Error de validación")
                                .build();
                    }
                })
                .toList();

        String referencia = getReferenciaSeguimiento(request);

        SpeiValidationErrorResponse response = SpeiValidationErrorResponse.builder()
                .referenciaSeguimiento(referencia)
                .errores(errores)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // Maneja excepciones personalizadas de validación que lanzan mensajes concatenados con punto y coma (;)
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<SpeiValidationErrorResponse> handleCustomValidationExceptions(RuntimeException ex, HttpServletRequest request) {
        String mensajeCompleto = ex.getMessage() != null ? ex.getMessage() : "";
        String[] partes = mensajeCompleto.split(";");

        List<ItemErrorResponse> errores = new ArrayList<>();

        for (String parte : partes) {
            parte = parte.trim();
            if (parte.isEmpty()) continue;

            // Limpiamos el prefijo "Error de validación:" si viene incluido
            parte = parte.replaceAll("(?i)^error de validación\\s*:\\s*", "").trim();

            String codigo = "PRX-001";
            String campo = "general";
            String mensaje = parte;

            if (parte.contains(":")) {
                String[] subPartes = parte.split(":", 2);
                if (subPartes.length > 1) {
                    codigo = subPartes[0].trim();
                    mensaje = subPartes[1].trim();
                }
            }

            errores.add(ItemErrorResponse.builder()
                    .codigo(codigo)
                    .campo(campo)
                    .mensaje(mensaje)
                    .build());
        }

        if (errores.isEmpty()) {
            errores.add(ItemErrorResponse.builder()
                    .codigo("PRX-001")
                    .campo("general")
                    .mensaje(mensajeCompleto)
                    .build());
        }

        String referencia = getReferenciaSeguimiento(request);

        SpeiValidationErrorResponse response = SpeiValidationErrorResponse.builder()
                .referenciaSeguimiento(referencia)
                .errores(errores)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // Captura cualquier otro error no contemplado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ex.printStackTrace();

        ErrorResponse error = ErrorResponse.builder()
                .codigo("PRX-999")
                .mensaje("Error interno del servidor")
                .detalles(List.of(ex.getMessage() != null ? ex.getMessage() : "Error desconocido"))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    // Método auxiliar seguro para obtener la referencia de seguimiento de los headers
    private String getReferenciaSeguimiento(HttpServletRequest request) {
        try {
            if (request != null) {
                String ref = request.getHeader("referenciaSeguimiento");
                if (ref != null && !ref.isBlank()) {
                    return ref;
                }
            }
        } catch (Exception ignored) {}
        return "N/A";
    }
}