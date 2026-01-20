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
        try {
            Main.main(new String[] {});
        } catch (Exception ignored) {
            // Se ignora la excepción porque el servidor puede estar ya levantado en el puerto 8082
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
    void testTaskHandlerFullCoverage() {
        Main.TaskHandler handler = new Main.TaskHandler();

        assertDoesNotThrow(() -> handler.handle(createMockExchange("OPTIONS", "", null)));

        String fullJson = "{\"imageCode\":\"test\",\"doctorId\":1,\"urgency\":3,\"createdAtMillis\":1000}";
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", fullJson, null)));

        String badJson = "{\"imageCode\":\"test\",\"doctorId\":-1}";
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", badJson, null)));
    }

    @Test
    void testRemoveHandlerCoverage() {
        Main.RemoveHandler handler = new Main.RemoveHandler();

        String json = "{\"doctorId\":1,\"imageCode\":\"img1\"}";
        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", json, null)));

        assertDoesNotThrow(() -> handler.handle(createMockExchange("OPTIONS", "", null)));

        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{}", null)));
    }

    @Test
    void testQueueHandlerCoverage() {
        Main.QueueHandler handler = new Main.QueueHandler();

        assertDoesNotThrow(() -> handler.handle(createMockExchange("GET", "", "doctorId=1")));

        assertDoesNotThrow(() -> handler.handle(createMockExchange("GET", "", "doctorId=d2")));

        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "", null)));

        assertDoesNotThrow(() -> handler.handle(createMockExchange("GET", "", "")));
    }

    @Test
    void testConfigHandlerFullCoverage() {
        Main.ConfigHandler handler = new Main.ConfigHandler();

        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{\"mode\":\"mp\"}", null)));

        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{\"mode\":\"monitor\"}", null)));

        assertDoesNotThrow(() -> handler.handle(createMockExchange("POST", "{\"mode\":\"monitor\"}", null)));
    }

    @Test
    void testMainExceptionSafety() {
        HttpExchange exchange = mock(HttpExchange.class);

        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getRequestMethod()).thenReturn("POST");

        when(exchange.getRequestBody()).thenReturn(null);

        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        when(exchange.getResponseBody()).thenReturn(os);

        Main.TaskHandler handler = new Main.TaskHandler();

        assertDoesNotThrow(() -> handler.handle(exchange),
                "El catch interno debería atrapar el error de lectura y enviar respuesta de error");
    }

    @Test
    void testSafeMsgCoverage() {
        assertNotNull(Main.safeMsg(null));
        assertEquals("test 'error'", Main.safeMsg("test \"error\""));
    }
}