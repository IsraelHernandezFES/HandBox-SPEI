package com.sandbox.spei.Validation.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandbox.spei.Validation.dto.response.OperacionResponse;
import com.sandbox.spei.Validation.entity.RegistroIdempotencia;
import com.sandbox.spei.Validation.exception.SpeiException;
import com.sandbox.spei.Validation.repository.RegistroIdempotenciaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Optional;

@Service
public class IdempotenciaService {

    private final RegistroIdempotenciaRepository idempotenciaRepository;
    private final ObjectMapper objectMapper;

    public IdempotenciaService(RegistroIdempotenciaRepository idempotenciaRepository, ObjectMapper objectMapper) {
        this.idempotenciaRepository = idempotenciaRepository;
        this.objectMapper = objectMapper;
    }

    /**
     * Valida el encabezado Clave-Idempotencia:
     * - Si no viene encabezado: retorna vacío para procesar normal.
     * - Si la clave es nueva: retorna vacío para procesar y crear.
     * - Si la clave existe y el body es idéntico: retorna la respuesta previa (para devolver 200 OK).
     * - Si la clave existe y el body cambió: lanza 409 CONFLICT con PRX-015.
     */
    public Optional<OperacionResponse> verificarIdempotencia(String claveIdempotencia, Object requestBody) {
        if (claveIdempotencia == null || claveIdempotencia.isBlank()) {
            return Optional.empty();
        }

        String hashActual = calcularSha256(requestBody);
        Optional<RegistroIdempotencia> existente = idempotenciaRepository.findById(claveIdempotencia);

        if (existente.isPresent()) {
            RegistroIdempotencia registro = existente.get();

            // Clave repetida con cuerpo distinto -> 409 PRX-015
            if (!registro.getRequestHash().equals(hashActual)) {
                throw new SpeiException(
                        "PRX-015",
                        "Clave de idempotencia ya utilizada con un cuerpo de solicitud diferente",
                        HttpStatus.CONFLICT
                );
            }

            // Clave repetida con cuerpo idéntico -> Retorna la respuesta original
            try {
                OperacionResponse respuestaOriginal = objectMapper.readValue(registro.getResponseBody(), OperacionResponse.class);
                return Optional.of(respuestaOriginal);
            } catch (Exception e) {
                throw new RuntimeException("Error al deserializar respuesta previa de idempotencia", e);
            }
        }

        return Optional.empty();
    }

    /**
     * Guarda la respuesta generada para responder a reintentos futuros con la misma clave.
     */
    public void registrarOperacion(String claveIdempotencia, Object requestBody, OperacionResponse respuesta, int statusCode) {
        if (claveIdempotencia == null || claveIdempotencia.isBlank()) {
            return;
        }

        try {
            String hash = calcularSha256(requestBody);
            String respuestaJson = objectMapper.writeValueAsString(respuesta);

            RegistroIdempotencia registro = RegistroIdempotencia.builder()
                    .claveIdempotencia(claveIdempotencia)
                    .requestHash(hash)
                    .responseBody(respuestaJson) // Corregido a camelCase
                    .statusCode(statusCode)
                    .build();

            idempotenciaRepository.save(registro);
        } catch (Exception e) {
            System.err.println("DETALLE ERROR IDEMPOTENCIA: " + (e.getCause() != null ? e.getCause().getMessage() : e.getMessage()));
            throw new RuntimeException("Error al registrar idempotencia", e);
        }
    }

    private String calcularSha256(Object body) {
        try {
            String json = objectMapper.writeValueAsString(body);
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(json.getBytes(StandardCharsets.UTF_8));

            StringBuilder hexString = new StringBuilder();
            for (byte b : encodedHash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException | com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException("Error al calcular hash de idempotencia", e);
        }
    }
}