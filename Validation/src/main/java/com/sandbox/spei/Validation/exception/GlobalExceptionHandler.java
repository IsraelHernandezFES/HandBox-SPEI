package com.sandbox.spei.Validation.exception;

//Menejo globar con @RestControllerAdvice que captura las excepciones de negocio , los errores de Validacion (bean) y fallos imprevistos

import com.sandbox.spei.Validation.dto.response.ErrorResponse;
import com.sandbox.spei.Validation.dto.response.ItemErrorResponse;
import com.sandbox.spei.Validation.dto.response.SpeiValidationErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
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

    // Maneja validaciones fallidas de anotaciones (@Valid, @NotNull, @Size, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<SpeiValidationErrorResponse> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        List<ItemErrorResponse> errores = ex.getBindingResult().getFieldErrors().stream()
                .map(err -> {
                    String defaultMsg = err.getDefaultMessage() != null ? err.getDefaultMessage() : "";
                    String codigo = "PRX-001";
                    String mensaje = defaultMsg;

                    if (defaultMsg.contains(":")) {
                        String[] parts = defaultMsg.split(":", 2);
                        codigo = parts[0];
                        mensaje = parts[1];
                    }

                    return ItemErrorResponse.builder()
                            .codigo(codigo)
                            .campo(err.getField())
                            .mensaje(mensaje)
                            .build();
                })
                .toList();

        SpeiValidationErrorResponse response = SpeiValidationErrorResponse.builder()
                .referenciaSeguimiento(request.getHeader("referenciaSeguimiento"))
                .errores(errores)
                .build();

        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(response);
    }

    // Captura cualquier otro error no contemplado
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
                .codigo("PRX-999")
                .mensaje("Error interno del servidor")
                .detalles(List.of(ex.getMessage()))
                .timestamp(LocalDateTime.now())
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }
}
