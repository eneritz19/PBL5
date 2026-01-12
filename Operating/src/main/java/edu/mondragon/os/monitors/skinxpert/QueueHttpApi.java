package edu.mondragon.os.monitors.skinxpert;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

public class QueueHttpApi {
    private final QueueEngine engine;
    private HttpServer server;

    public QueueHttpApi(QueueEngine engine) {
        this.engine = engine;
    }

    public void start(int port) throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/enqueue", this::handleEnqueue);
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        server.start();
        System.out.println("[HTTP] Listening on " + port + " (POST /enqueue)");
    }

    private void handleEnqueue(HttpExchange ex) throws IOException {
        if (!"POST".equalsIgnoreCase(ex.getRequestMethod())) {
            send(ex, 405, "Method Not Allowed");
            return;
        }
        String body = readAll(ex.getRequestBody());

        String imageCode = extractJson(body, "imageCode");
        String doctorId  = extractJson(body, "doctorId");
        String urgencyS  = extractJson(body, "urgency");

        if (imageCode == null || doctorId == null || urgencyS == null) {
            send(ex, 400, "Bad Request: missing fields");
            return;
        }

        PhotoMsg.Urgency urgency;
        try {
            urgency = PhotoMsg.Urgency.valueOf(urgencyS);
        } catch (Exception e) {
            send(ex, 400, "Bad Request: urgency must be ALTO|MEDIO|BAJO");
            return;
        }

        try {
            engine.onIncoming(new PhotoMsg(imageCode, doctorId, urgency));
            send(ex, 200, "OK");
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            send(ex, 500, "Interrupted");
        }
    }

    private static String readAll(InputStream in) throws IOException {
        return new String(in.readAllBytes(), StandardCharsets.UTF_8);
    }

    private static void send(HttpExchange ex, int code, String text) throws IOException {
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().add("Content-Type", "text/plain; charset=utf-8");
        ex.sendResponseHeaders(code, bytes.length);
        ex.getResponseBody().write(bytes);
        ex.close();
    }

    private static String extractJson(String json, String key) {
        String needle = "\"" + key + "\"";
        int i = json.indexOf(needle);
        if (i < 0) return null;
        int colon = json.indexOf(':', i);
        if (colon < 0) return null;
        int firstQuote = json.indexOf('"', colon + 1);
        if (firstQuote < 0) return null;
        int secondQuote = json.indexOf('"', firstQuote + 1);
        if (secondQuote < 0) return null;
        return json.substring(firstQuote + 1, secondQuote).trim();
    }
}

