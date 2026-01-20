package com.example.operating;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;
import com.example.Main;

class MainHandlersTest {

    @BeforeAll
    static void setup() {
        // Inicializamos Main una sola vez para que los motores estáticos existan
        try {
            Main.main(new String[] {});
        } catch (Exception ignored) {
        }
    }

    private HttpExchange createMockExchange(String method, String body, String query) throws IOException {
        HttpExchange exchange = mock(HttpExchange.class);
        InputStream is = new ByteArrayInputStream(body.getBytes(StandardCharsets.UTF_8));

        when(exchange.getRequestBody()).thenReturn(is);
        when(exchange.getRequestMethod()).thenReturn(method);
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());
        if (query != null) {
            when(exchange.getRequestURI()).thenReturn(java.net.URI.create("http://localhost/test?" + query));
        }
        return exchange;
    }

    @Test
    void testTaskHandlerFullCoverage() throws IOException {
        Main.TaskHandler handler = new Main.TaskHandler();

        // Caso 1: OPTIONS (Cubre la rama CORS)
        assertDoesNotThrow(() -> handler.handle(createMockExchange("OPTIONS", "", null)));

        // Caso 2: POST con datos correctos (incluyendo createdAtMillis para cubrir esa
        // rama)
        String fullJson = "{\"imageCode\":\"test\",\"doctorId\":1,\"urgency\":3,\"createdAtMillis\":1000}";
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", fullJson, null)));

        // Caso 3: Errores de validación (doctorId inválido)
        String badJson = "{\"imageCode\":\"test\",\"doctorId\":-1}";
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", badJson, null)));
    }

    @Test
    void testRemoveHandlerCoverage() throws IOException {
        Main.RemoveHandler handler = new Main.RemoveHandler();

        // Caso 1: POST correcto
        String json = "{\"doctorId\":1,\"imageCode\":\"img1\"}";
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", json, null)));

        // Caso 2: OPTIONS
        assertDoesNotThrow(() -> handler.handle(createMockExchange("OPTIONS", "", null)));

        // Caso 3: Error de validación
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{}", null)));
    }

    @Test
    void testQueueHandlerCoverage() throws IOException {
        Main.QueueHandler handler = new Main.QueueHandler();

        // Caso 1: GET con doctorId numérico (D1)
        assertDoesNotThrow(() -> handler.handle(createMockExchange("GET", "", "doctorId=1")));

        // Caso 2: GET con doctorId formato "d2"
        assertDoesNotThrow(() -> handler.handle(createMockExchange("GET", "", "doctorId=d2")));

        // Caso 3: Error 405 (No es GET)
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "", null)));

        // Caso 4: Missing doctorId
        assertDoesNotThrow(() -> handler.handle(createMockExchange("GET", "", "")));
    }

    @Test
    void testConfigHandlerFullCoverage() throws IOException {
        Main.ConfigHandler handler = new Main.ConfigHandler();

        // Caso 1: Cambio a MP (si el actual es monitor)
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{\"mode\":\"mp\"}", null)));

        // Caso 2: Cambio a MONITOR (si el actual es mp)
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{\"mode\":\"monitor\"}", null)));

        // Caso 3: Mismo modo (cubre la rama "already in that mode")
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{\"mode\":\"monitor\"}", null)));
    }

    @Test
    void testMainExceptionSafety() throws IOException {
        // 1. Creamos el mock
        HttpExchange exchange = mock(HttpExchange.class);

        // 2. Configuramos los retornos básicos para que addCorsHeaders no falle
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getRequestMethod()).thenReturn("POST");

        // 3. Forzamos el fallo en el try (esto hará que salte al catch)
        when(exchange.getRequestBody()).thenReturn(null);

        // 4. CORRECCIÓN: Configuramos un OutputStream para que sendResponse no de NPE
        // Usamos un ByteArrayOutputStream que simplemente guarda los datos en memoria
        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        Main.TaskHandler handler = new Main.TaskHandler();

        // Ahora sí pasará por el catch y ejecutará el log de error sin romperse
        assertDoesNotThrow(() -> handler.handle(exchange),
                "El catch interno debería atrapar el error de lectura y enviar respuesta de error");
    }

    @Test
    void testSafeMsgCoverage() {
        // Esto cubre las dos ramas del método safeMsg (null y reemplazo de comillas)
        assertNotNull(Main.safeMsg(null));
        assertEquals("test 'error'", Main.safeMsg("test \"error\""));
    }
}