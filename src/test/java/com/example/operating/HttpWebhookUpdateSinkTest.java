package com.example.operating;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import com.example.HttpWebhookUpdateSink;
import com.example.QueueUpdate;

class HttpWebhookUpdateSinkTest {

    private MockWebServer server;

    @AfterEach
    void tearDown() throws Exception {
        if (server != null)
            server.shutdown();
    }

    @Test
    void push_sendsPostWithJsonBodyAndContentType() throws Exception {
        server = new MockWebServer();
        server.enqueue(new MockResponse().setResponseCode(204));
        server.start();

        String url = server.url("/webhook").toString();
        HttpWebhookUpdateSink sink = new HttpWebhookUpdateSink(url);

        List<QueueUpdate.QueueItem> queue = List.of(
                new QueueUpdate.QueueItem("img1", "ALTO", 10L),
                new QueueUpdate.QueueItem("img2", "MEDIO", 20L));

        Map<String, Integer> sizes = new LinkedHashMap<>();
        sizes.put("ALTO", 1);
        sizes.put("MEDIO", 1);
        sizes.put("BAJO", 0);
        sizes.put("TOTAL", 2);

        QueueUpdate update = new QueueUpdate("D1", queue, sizes);

        sink.push(update);

        RecordedRequest req = server.takeRequest(2, TimeUnit.SECONDS);
        assertNotNull(req, "Expected a request to arrive to the webhook");

        assertEquals("POST", req.getMethod());
        assertEquals("/webhook", req.getPath());

        String contentType = req.getHeader("Content-Type");
        assertNotNull(contentType);
        assertTrue(contentType.toLowerCase().contains("application/json"));

        String body = req.getBody().readString(StandardCharsets.UTF_8);

        String expected = "{\"doctorId\":\"D1\"," +
                "\"queue\":[" +
                "{\"imageCode\":\"img1\",\"urgency\":\"ALTO\",\"createdAt\":10}," +
                "{\"imageCode\":\"img2\",\"urgency\":\"MEDIO\",\"createdAt\":20}" +
                "]," +
                "\"sizes\":{" +
                "\"ALTO\":1,\"MEDIO\":1,\"BAJO\":0,\"TOTAL\":2" +
                "}" +
                "}";

        assertEquals(expected, body);
    }

    @Test
    void push_doesNotThrowIfServerIsDown_printsErrorToStderr() {
        String url = "http://localhost:1/webhook";
        HttpWebhookUpdateSink sink = new HttpWebhookUpdateSink(url);

        QueueUpdate update = new QueueUpdate("D9", List.of(), Map.of());

        assertDoesNotThrow(() -> sink.push(update),
                "El sink debe gestionar el error de conexión internamente sin lanzar excepciones");
    }

    @Test
    void push_whenInterrupted_restoresStatus() {
        HttpWebhookUpdateSink sink = new HttpWebhookUpdateSink("http://localhost:1234");
        Thread.currentThread().interrupt(); 
        sink.push(new QueueUpdate("D1", List.of(), Map.of()));
        assertTrue(Thread.interrupted()); 
    }
}