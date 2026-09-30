package com.sandbox.spei.Validation;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class OrdenPagoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    public void deberiaCrearOperacionExitosamente() throws Exception {
        Map<String, Object> request = Map.of(
                "tipoOperacion", "T2T",
                "referenciaSeguimiento", "TESTAUTO001",
                "concepto", "Prueba automatizada exitosa",
                "folioNumerico", 9991,
                "importe", Map.of("valor", 250.00, "divisa", "MXN"),
                "emisor", Map.of("institucion", "801", "cuenta", "801180000118359717", "nombre", "Emisor Test"),
                "receptor", Map.of("institucion", "802", "cuenta", "802180000123456701", "nombre", "Receptor Test")
        );

        mockMvc.perform(post("/api/v1/operaciones")
                        .header("Clave-Idempotencia", "IDEMPOTENCIA-AUTO-01")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claveRastreo").value("TESTAUTO001"))
                .andExpect(jsonPath("$.estadoActual").value("S03"));
    }

    @Test
    public void deberiaRetornarIdempotenciaSiSeRepiteClave() throws Exception {
        Map<String, Object> request = Map.of(
                "tipoOperacion", "T2T",
                "referenciaSeguimiento", "TESTAUTO002",
                "concepto", "Prueba idempotencia",
                "folioNumerico", 9992,
                "importe", Map.of("valor", 300.00, "divisa", "MXN"),
                "emisor", Map.of("institucion", "801", "cuenta", "801180000118359717", "nombre", "Emisor Test"),
                "receptor", Map.of("institucion", "802", "cuenta", "802180000123456701", "nombre", "Receptor Test")
        );

        // Primera petición (Crea la operación y devuelve 201)
        mockMvc.perform(post("/api/v1/operaciones")
                        .header("Clave-Idempotencia", "IDEMPOTENCIA-AUTO-02")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated());

        // Segunda petición con la MISMA clave de idempotencia
        // Validamos que devuelva 200 OK (caché) en lugar de crear un recurso nuevo
        mockMvc.perform(post("/api/v1/operaciones")
                        .header("Clave-Idempotencia", "IDEMPOTENCIA-AUTO-02")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());
    }
    @Test
    public void deberiaForzarEscenarioFondosInsuficientes() throws Exception {
        Map<String, Object> request = Map.of(
                "tipoOperacion", "T2T",
                "referenciaSeguimiento", "TESTAUTO003",
                "concepto", "Prueba sandbox forzada",
                "folioNumerico", 9993,
                "importe", Map.of("valor", 500.00, "divisa", "MXN"),
                "emisor", Map.of("institucion", "801", "cuenta", "801180000118359717", "nombre", "Emisor Test"),
                "receptor", Map.of("institucion", "802", "cuenta", "802180000123456701", "nombre", "Receptor Test")
        );

        // Enviamos el header X-Escenario-Forzado con PRX-020
        mockMvc.perform(post("/api/v1/operaciones")
                        .header("Clave-Idempotencia", "IDEMPOTENCIA-AUTO-03")
                        .header("X-Escenario-Forzado", "PRX-020")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estadoActual").value("S04")) // Debe ser devuelto
                .andExpect(jsonPath("$.codigoMotivo").value("PRX-020"));
    }

    @Test
    public void deberiaRechazarPorInstitucionInexistente() throws Exception {
        Map<String, Object> request = Map.of(
                "tipoOperacion", "T2T",
                "referenciaSeguimiento", "TESTAUTO004",
                "concepto", "Prueba error catalogo",
                "folioNumerico", 9994,
                "importe", Map.of("valor", 100.00, "divisa", "MXN"),
                // Usamos la cuenta terminada en 2 para que pase la validación matemática (PRX-002)
                // y fuerce la validación de base de datos (PRX-003)
                "emisor", Map.of("institucion", "999", "cuenta", "999180000000000002", "nombre", "Emisor Falso"),
                "receptor", Map.of("institucion", "802", "cuenta", "802180000123456701", "nombre", "Receptor Test")
        );

        mockMvc.perform(post("/api/v1/operaciones")
                        .header("Clave-Idempotencia", "IDEMPOTENCIA-AUTO-04")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnprocessableEntity()) // 422
                .andExpect(jsonPath("$.codigo").value("PRX-003"));
    }
}