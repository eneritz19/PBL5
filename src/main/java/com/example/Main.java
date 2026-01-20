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
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final String MODE_MONITOR = "monitor";
    private static final String MODE_MP = "mp";
    private static final String METHOD_OPTIONS = "OPTIONS";
    private static final String ERROR_MSG_GENERIC = "Error procesando petición";
    private static final String JSON_ERROR_PREFIX = "{\"error\":\"";

    private static Engine monitorEngine;
    private static Engine mpEngine;
    private static ActiveEngine active;
    private static final Gson gson = new Gson();
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());

    private static UpdateSink sink;
    private static final Object modeLock = new Object();
    private static final int PER_DOCTOR_CAPACITY = 20;

    public static class ActiveEngine {
        final AtomicReference<Engine> current = new AtomicReference<>();
        final AtomicReference<String> mode = new AtomicReference<>(MODE_MONITOR);
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando SkinXpert Operating Service (HTTP)...");

        sink = new ConsoleUpdateSink();

        monitorEngine = new MonitorEngine(new DoctorQueueManager(PER_DOCTOR_CAPACITY), sink);
        mpEngine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);

        active = new ActiveEngine();
        active.current.set(monitorEngine);
        active.mode.set(MODE_MONITOR);

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
    }

    public static class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if (METHOD_OPTIONS.equalsIgnoreCase(exchange.getRequestMethod())) {
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
                    sendResponse(exchange, 400, JSON_ERROR_PREFIX + "Invalid JSON or missing imageCode\"}");
                    return;
                }
                if (dto.doctorId <= 0) {
                    sendResponse(exchange, 400, JSON_ERROR_PREFIX + "Invalid doctorId\"}");
                    return;
                }

                String doctorIdStr = "D" + dto.doctorId;
                
                PhotoMsg.Urgency urgencyEnum;
                if (dto.urgency >= 3) {
                    urgencyEnum = PhotoMsg.Urgency.ALTO;
                } else {
                    urgencyEnum = (dto.urgency == 2) ? PhotoMsg.Urgency.MEDIO : PhotoMsg.Urgency.BAJO;
                }

                PhotoMsg photo = (dto.createdAtMillis != null)
                        ? new PhotoMsg(dto.imageCode, doctorIdStr, urgencyEnum, dto.createdAtMillis)
                        : new PhotoMsg(dto.imageCode, doctorIdStr, urgencyEnum);

                Engine engineSnapshot;
                String modeSnapshot;
                synchronized (modeLock) {
                    engineSnapshot = active.current.get();
                    modeSnapshot = active.mode.get();
                }

                engineSnapshot.accept(photo);
                sendResponse(exchange, 200, "{\"status\":\"ok\",\"mode\":\"" + modeSnapshot + "\"}");

            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Interrumpido durante add-task", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, ERROR_MSG_GENERIC, e);
                sendResponse(exchange, 500, JSON_ERROR_PREFIX + safeMsg(e.getMessage()) + "\"}");
            }
        }
    }

    public static class RemoveHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if (METHOD_OPTIONS.equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            
            processRemoveRequest(exchange);
        }

        private void processRemoveRequest(HttpExchange exchange) throws IOException {
            try {
                String body = readBody(exchange);
                RemoveDto dto = gson.fromJson(body, RemoveDto.class);

                if (dto == null || dto.imageCode == null || dto.doctorId <= 0) {
                    sendResponse(exchange, 400, JSON_ERROR_PREFIX + "Invalid JSON (doctorId, imageCode required)\"}");
                    return;
                }

                Engine engineSnapshot;
                synchronized (modeLock) {
                    engineSnapshot = active.current.get();
                }

                boolean removed = engineSnapshot.remove("D" + dto.doctorId, dto.imageCode);
                sendResponse(exchange, 200, "{\"removed\":" + removed + "}");

            } catch (InterruptedException ie) {
                LOGGER.log(Level.SEVERE, "Interrumpido en remove", ie);
                Thread.currentThread().interrupt();
                sendResponse(exchange, 500, JSON_ERROR_PREFIX + "interrupted\"}");
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, ERROR_MSG_GENERIC, e);
                sendResponse(exchange, 500, JSON_ERROR_PREFIX + safeMsg(e.getMessage()) + "\"}");
            }
        }
    }

    public static class QueueHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                return;
            }

            try {
                String q = exchange.getRequestURI().getQuery();
                String raw = extractQuery(q, "doctorId");

                if (raw == null || raw.isBlank()) {
                    sendResponse(exchange, 400, JSON_ERROR_PREFIX + "Missing/invalid doctorId\"}");
                    return;
                }

                String doctorIdStr = raw.trim().matches("(?i)^d\\d+$") ? raw.trim().toUpperCase() : "D" + raw.trim();

                Engine engineSnapshot;
                String modeSnapshot;
                synchronized (modeLock) {
                    engineSnapshot = active.current.get();
                    modeSnapshot = active.mode.get();
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
                LOGGER.log(Level.SEVERE, ERROR_MSG_GENERIC, e);
                sendResponse(exchange, 500, JSON_ERROR_PREFIX + safeMsg(e.getMessage()) + "\"}");
            }
        }

        private String extractQuery(String query, String key) {
            if (query == null || query.isBlank()) return null;
            for (String p : query.split("&")) {
                String[] kv = p.split("=", 2);
                if (kv.length == 2 && kv[0].equals(key)) return kv[1];
            }
            return null;
        }
    }

    public static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            String stateSnapshot;
            String modeSnapshot;
            synchronized (modeLock) {
                stateSnapshot = active.current.get().state();
                modeSnapshot = active.mode.get();
            }
            sendResponse(exchange, 200, gson.toJson(Map.of("mode", modeSnapshot, "state", stateSnapshot)));
        }
    }

    public static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if (METHOD_OPTIONS.equalsIgnoreCase(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }

            try {
                String body = readBody(exchange);
                ConfigDto config = gson.fromJson(body, ConfigDto.class);
                String desired = (config != null && config.mode != null) ? config.mode.trim().toLowerCase() : "";

                if (!desired.equals(MODE_MP) && !desired.equals(MODE_MONITOR)) {
                    sendResponse(exchange, 400, JSON_ERROR_PREFIX + "mode must be monitor|mp\"}");
                    return;
                }

                synchronized (modeLock) {
                    if (!desired.equals(active.mode.get())) {
                        Map<String, List<QueueUpdate.QueueItem>> state = active.current.get().dumpAll();
                        active.current.get().shutdown();

                        if (desired.equals(MODE_MP)) {
                            mpEngine = new MessagePassingEngine(new MPDoctorQueueManager(), sink);
                            mpEngine.loadAll(state);
                            active.current.set(mpEngine);
                            active.mode.set(MODE_MP);
                        } else {
                            monitorEngine = new MonitorEngine(new DoctorQueueManager(PER_DOCTOR_CAPACITY), sink);
                            monitorEngine.loadAll(state);
                            active.current.set(monitorEngine);
                            active.mode.set(MODE_MONITOR);
                        }
                    }
                }
                sendResponse(exchange, 200, "{\"current_mode\":\"" + active.mode.get() + "\"}");

            } catch (InterruptedException e) {
                LOGGER.log(Level.SEVERE, "Interrupcion en config", e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, ERROR_MSG_GENERIC, e);
                sendResponse(exchange, 500, JSON_ERROR_PREFIX + safeMsg(e.getMessage()) + "\"}");
            }
        }
    }

    private static String readBody(HttpExchange exchange) throws IOException {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8))) {
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
        try (OutputStream os = exchange.getResponseBody()) { os.write(bytes); }
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    public static String safeMsg(String msg) {
        return (msg == null) ? "unknown" : msg.replace("\"", "'");
    }

    public static class TaskDto { 
        String imageCode; 
        int doctorId; 
        int urgency; 
        Long createdAtMillis; 
    }
    
    public static class RemoveDto { 
        int doctorId; 
        String imageCode; 
    }
    
    public static class ConfigDto { 
        String mode; 
    }
}