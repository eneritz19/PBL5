package com.example;
import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

public class Main {

    // --- GLOBALS ---
    private static Engine monitorEngine;
    private static Engine mpEngine;
    private static ActiveEngine active;
    private static final Gson gson = new Gson();
    private static final java.util.logging.Logger LOGGER = java.util.logging.Logger.getLogger(Main.class.getName());

    // sink global (para recrear engines en migración)
    private static UpdateSink sink;

    // Lock global: evita carreras entre /config y /add-task /remove-task /queue
    private static final Object modeLock = new Object();

    // Capacity monitor inbox
    private static final int PER_DOCTOR_CAPACITY = 20;

    static class ActiveEngine {
        volatile Engine current;
        volatile String mode = "monitor";
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando SkinXpert Operating Service (HTTP)...");

        // 1) Sink (si quieres push real, cambia a HttpWebhookUpdateSink)
        sink = new ConsoleUpdateSink();

        // 2) Engines iniciales (vacíos)
        monitorEngine = new MonitorEngine(new DoctorQueueManager(PER_DOCTOR_CAPACITY), sink);
        mpEngine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        // 3) Activo por defecto
        active = new ActiveEngine();
        active.current = monitorEngine;
        active.mode = "monitor";

        // 4) HTTP server
        int port = 8082;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/add-task", new TaskHandler());
        server.createContext("/remove-task", new RemoveHandler());
        server.createContext("/queue", new QueueHandler());
        server.createContext("/status", new StatusHandler());
        server.createContext("/config", new ConfigHandler());

        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Servidor escuchando en: http://localhost:" + port);
        System.out.println("Modo inicial: " + active.mode);
        System.out.println("Endpoints:");
        System.out.println("  POST /add-task      -> {imageCode, doctorId, urgency, createdAtMillis?}");
        System.out.println("  POST /remove-task   -> {doctorId, imageCode}");
        System.out.println("  GET  /queue?doctorId=1");
        System.out.println("  GET  /status");
        System.out.println("  POST /config        -> {mode: monitor|mp}  (migra el estado)");
    }

    // =========================================================================
    // HANDLERS
    // =========================================================================

    /**
     * POST /add-task
     * JSON esperado: { "imageCode": "img1", "doctorId": 1, "urgency": 3, "createdAtMillis": 123? }
     */
    static class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                String body = readBody(exchange);
                TaskDto dto = gson.fromJson(body, TaskDto.class);

                if (dto == null || dto.imageCode == null || dto.imageCode.isBlank()) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid JSON or missing imageCode\"}");
                    return;
                }
                if (dto.doctorId <= 0) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid doctorId\"}");
                    return;
                }

                String doctorIdStr = "D" + dto.doctorId;

                PhotoMsg.Urgency urgencyEnum;
                if (dto.urgency >= 3) urgencyEnum = PhotoMsg.Urgency.ALTO;
                else if (dto.urgency == 2) urgencyEnum = PhotoMsg.Urgency.MEDIO;
                else urgencyEnum = PhotoMsg.Urgency.BAJO;

                PhotoMsg photo = (dto.createdAtMillis != null)
                        ? new PhotoMsg(dto.imageCode, doctorIdStr, urgencyEnum, dto.createdAtMillis)
                        : new PhotoMsg(dto.imageCode, doctorIdStr, urgencyEnum);

                Engine engineSnapshot;
                String modeSnapshot;
                synchronized (modeLock) {
                    engineSnapshot = active.current;
                    modeSnapshot = active.mode;
                }

                engineSnapshot.accept(photo);

                sendResponse(exchange, 200, "{\"status\":\"ok\",\"mode\":\"" + modeSnapshot + "\"}");
                System.out.println("[HTTP] add-task: " + dto.imageCode + " -> " + doctorIdStr +
                        " (" + urgencyEnum + ") mode=" + modeSnapshot);

            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Error procesando petición", e);
                sendResponse(exchange, 500, "{\"error\":\"" + safeMsg(e.getMessage()) + "\"}");
            }
        }
    }

    /**
     * POST /remove-task
     * JSON: { "doctorId": 1, "imageCode": "xxx.jpg" }
     */
    static class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                String body = readBody(exchange);
                RemoveDto dto = gson.fromJson(body, RemoveDto.class);

                if (dto == null || dto.imageCode == null || dto.imageCode.isBlank() || dto.doctorId <= 0) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid JSON (doctorId, imageCode required)\"}");
                    return;
                }

                String doctorIdStr = "D" + dto.doctorId;

                Engine engineSnapshot;
                synchronized (modeLock) {
                    engineSnapshot = active.current;
                }

                boolean removed;
                try {
                    removed = engineSnapshot.remove(doctorIdStr, dto.imageCode);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    sendResponse(exchange, 500, "{\"error\":\"interrupted\"}");
                    return;
                }

                sendResponse(exchange, 200, "{\"removed\":" + removed + "}");
                System.out.println("[HTTP] remove-task: " + dto.imageCode + " from " + doctorIdStr + " -> " + removed);

            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Error procesando petición", e);
                sendResponse(exchange, 500, "{\"error\":\"" + safeMsg(e.getMessage()) + "\"}");
            }
        }
    }

    /**
     * GET /queue?doctorId=1
     * Devuelve la cola REAL ordenada (lo que Java usa internamente).
     */
    static class QueueHandler implements HttpHandler {
    @Override
    public void handle(HttpExchange exchange) throws IOException {
        addCorsHeaders(exchange);

        if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
            exchange.sendResponseHeaders(405, -1);
            return;
        }

        try {
            String q = exchange.getRequestURI().getQuery(); // e.g. "doctorId=D1"
            String raw = extractQuery(q, "doctorId");

            if (raw == null || raw.isBlank()) {
                sendResponse(exchange, 400, "{\"error\":\"Missing/invalid doctorId\"}");
                return;
            }

            raw = raw.trim();
            String doctorIdStr;

            // ✅ Acepta "D1" o "d1"
            if (raw.matches("(?i)^d\\d+$")) {
                doctorIdStr = raw.toUpperCase();
            }
            // ✅ Acepta "1"
            else if (raw.matches("^\\d+$")) {
                doctorIdStr = "D" + raw;
            } else {
                sendResponse(exchange, 400, "{\"error\":\"Missing/invalid doctorId\"}");
                return;
            }

            Engine engineSnapshot;
            String modeSnapshot;
            synchronized (modeLock) {
                engineSnapshot = active.current;
                modeSnapshot = active.mode;
            }

            QueueUpdate update = engineSnapshot.getQueue(doctorIdStr);

            Map<String, Object> payload = Map.of(
                    "mode", modeSnapshot,
                    "doctorId", update.doctorId,
                    "queue", update.queueOrdered,
                    "sizes", update.sizes
            );

            sendResponse(exchange, 200, gson.toJson(payload));

        } catch (Exception e) {
            LOGGER.log(java.util.logging.Level.SEVERE, "Error procesando petición", e);
            sendResponse(exchange, 500, "{\"error\":\"" + safeMsg(e.getMessage()) + "\"}");
        }
    }
}

// helper nuevo (simple)
private static String extractQuery(String query, String key) {
    if (query == null || query.isBlank()) return null;
    for (String p : query.split("&")) {
        String[] kv = p.split("=", 2);
        if (kv.length == 2 && kv[0].equals(key)) return kv[1];
    }
    return null;
}


    /**
     * GET /status
     * Devuelve estado (string) + mode
     */
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            String stateSnapshot;
            String modeSnapshot;
            synchronized (modeLock) {
                stateSnapshot = active.current.state();
                modeSnapshot = active.mode;
            }

            Map<String, Object> payload = Map.of(
                    "mode", modeSnapshot,
                    "state", stateSnapshot
            );

            sendResponse(exchange, 200, gson.toJson(payload));
        }
    }

    /**
     * POST /config
     * JSON: { "mode": "monitor" | "mp" }
     *
     * ✅ Migra el estado: dump del engine actual -> crea engine nuevo -> loadAll(state)
     */
    static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("OPTIONS".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                String body = readBody(exchange);
                ConfigDto config = gson.fromJson(body, ConfigDto.class);

                if (config == null || config.mode == null) {
                    sendResponse(exchange, 400, "{\"error\":\"Missing mode\"}");
                    return;
                }

                String desired = config.mode.trim().toLowerCase();
                if (!desired.equals("mp") && !desired.equals("monitor")) {
                    sendResponse(exchange, 400, "{\"error\":\"mode must be monitor|mp\"}");
                    return;
                }

                synchronized (modeLock) {
                    if (desired.equals(active.mode)) {
                        sendResponse(exchange, 200, "{\"current_mode\":\"" + active.mode + "\",\"note\":\"already in that mode\"}");
                        return;
                    }

                    // 1) Dump estado del engine actual
                    Map<String, List<QueueUpdate.QueueItem>> state = active.current.dumpAll();

                    // 2) Apaga el engine viejo (importante en MP por threads)
                    active.current.shutdown();

                    // 3) Crea engine NUEVO en el modo deseado (vacío) y carga estado
                    if (desired.equals("mp")) {
                        mpEngine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);
                        mpEngine.loadAll(state);
                        active.current = mpEngine;
                        active.mode = "mp";
                    } else {
                        monitorEngine = new MonitorEngine(new DoctorQueueManager(PER_DOCTOR_CAPACITY), sink);
                        monitorEngine.loadAll(state);
                        active.current = monitorEngine;
                        active.mode = "monitor";
                    }
                }

                System.out.println("[HTTP] config: mode changed to " + active.mode + " (state migrated)");
                sendResponse(exchange, 200,
                        "{\"current_mode\":\"" + active.mode + "\",\"note\":\"state migrated\"}");

            } catch (Exception e) {
                LOGGER.log(java.util.logging.Level.SEVERE, "Error procesando petición", e);
                sendResponse(exchange, 500, "{\"error\":\"" + safeMsg(e.getMessage()) + "\"}");
            }
        }
    }

    // =========================================================================
    // UTILIDADES
    // =========================================================================

    private static String readBody(HttpExchange exchange) throws IOException {
        try (InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
             BufferedReader br = new BufferedReader(isr)) {
            StringBuilder sb = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) sb.append(line);
            return sb.toString();
        }
    }

    private static void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        byte[] bytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        exchange.sendResponseHeaders(code, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    private static String safeMsg(String msg) {
        if (msg == null) return "unknown";
        return msg.replace("\"", "'");
    }

    private static int extractIntQuery(String query, String key, int def) {
        if (query == null || query.isBlank()) return def;
        String[] parts = query.split("&");
        for (String p : parts) {
            String[] kv = p.split("=");
            if (kv.length == 2 && kv[0].equals(key)) {
                try { return Integer.parseInt(kv[1]); } catch (Exception ignored) {}
            }
        }
        return def;
    }

    // =========================================================================
    // DTOs
    // =========================================================================

    static class TaskDto {
        String imageCode;
        int doctorId;
        int urgency;
        Long createdAtMillis; // opcional (rehydrate/aging real)
    }

    static class RemoveDto {
        int doctorId;
        String imageCode;
    }

    static class ConfigDto {
        String mode;
    }
}
