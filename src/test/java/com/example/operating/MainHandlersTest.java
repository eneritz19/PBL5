package com.example.operating;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.Headers;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.Mockito.*;
import com.example.Main;

class MainHandlersTest {

    @Test
    void testHandlersCoverage() throws IOException {
        // Mock del intercambio HTTP
        HttpExchange exchange = mock(HttpExchange.class);
        
        // Simular un body JSON para /add-task
        String json = "{\"imageCode\":\"test\",\"doctorId\":1,\"urgency\":2}";
        InputStream is = new ByteArrayInputStream(json.getBytes(StandardCharsets.UTF_8));
        
        // Configuración de los Mocks
        when(exchange.getRequestBody()).thenReturn(is);
        when(exchange.getRequestMethod()).thenReturn("POST");
        when(exchange.getResponseHeaders()).thenReturn(new Headers());
        when(exchange.getResponseBody()).thenReturn(new ByteArrayOutputStream());

        // 1. Inicializar Main (para evitar NullPointerException en los motores)
        // Usamos un try-catch por si el puerto 8082 ya está ocupado en el runner
        try {
            Main.main(new String[]{});
        } catch (Exception ignored) { }

        // 2. Test TaskHandler (ahora público)
        Main.TaskHandler taskHandler = new Main.TaskHandler();
        assertDoesNotThrow(() -> taskHandler.handle(exchange));

        // 3. Test StatusHandler
        Main.StatusHandler statusHandler = new Main.StatusHandler();
        when(exchange.getRequestMethod()).thenReturn("GET");
        assertDoesNotThrow(() -> statusHandler.handle(exchange));
    }
}