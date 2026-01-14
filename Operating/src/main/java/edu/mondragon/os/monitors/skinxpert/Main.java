package edu.mondragon.os.monitors.skinxpert;

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
import java.util.Map;
import java.util.concurrent.Executors;

public class Main {

    // --- VARIABLES GLOBALES ---
    private static Engine monitorEngine;
    private static Engine mpEngine;
    private static ActiveEngine active;
    private static final Gson gson = new Gson();

    // Lock global: evita carreras entre /config y /add-task
    private static final Object modeLock = new Object();

    // Clase auxiliar para gestionar el cambio de modo en caliente
    static class ActiveEngine {
        volatile Engine current;
        volatile String mode = "monitor";
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando SkinXpert Operating Service (HTTP)...");

        // 1) Salida (por ahora consola; si quieres push real, cambia el sink)
        UpdateSink sink = new ConsoleUpdateSink();

        // 2) Inicialización de los dos Engines (Monitor y Message Passing)
        DoctorQueueManager monitorMgr = new DoctorQueueManager(20);
        monitorEngine = new MonitorEngine(monitorMgr, sink);

        MPDoctorQueueManager mpMgr = new MPDoctorQueueManager();
        mpEngine = new MessagePassingEngine(mpMgr, sink);

        // 3) Motor por defecto
        active = new ActiveEngine();
        active.current = monitorEngine;
        active.mode = "monitor";

        // 4) Servidor HTTP
        int port = 8082;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // Endpoints
        server.createContext("/add-task", new TaskHandler());
        server.createContext("/status", new StatusHandler());
        server.createContext("/config", new ConfigHandler());

        // ✅ IMPORTANTE: executor concurrente (mejor para OS y para atender varias peticiones)
        server.setExecutor(Executors.newCachedThreadPool());
        server.start();

        System.out.println("Servidor escuchando en: http://localhost:" + port);
        System.out.println("Modo inicial: " + active.mode);
        System.out.println("Endpoints:");
        System.out.println("  POST /add-task   -> {imageCode, doctorId, urgency}");
        System.out.println("  GET  /status     -> estado actual (JSON)");
        System.out.println("  POST /config     -> {mode: monitor|mp}");
        System.out.println("NOTA: cambiar de modo NO migra el estado entre colas.");
    }

    // =========================================================================
    // HANDLERS
    // =========================================================================

    /**
     * POST /add-task
     * JSON esperado: { "imageCode": "img1", "doctorId": 1, "urgency": 3 }
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
                if (dto == null || dto.imageCode == null) {
                    sendResponse(exchange, 400, "{\"error\":\"Invalid JSON or missing imageCode\"}");
                    return;
                }

                // Convertir ID médico (int -> "D1")
                String doctorIdStr = "D" + dto.doctorId;

                // Convertir urgencia numérica (3=ALTO, 2=MEDIO, 1=BAJO)
                PhotoMsg.Urgency urgencyEnum;
                if (dto.urgency >= 3) urgencyEnum = PhotoMsg.Urgency.ALTO;
                else if (dto.urgency == 2) urgencyEnum = PhotoMsg.Urgency.MEDIO;
                else urgencyEnum = PhotoMsg.Urgency.BAJO;

                PhotoMsg photo = new PhotoMsg(dto.imageCode, doctorIdStr, urgencyEnum);

                // Sección crítica: evita carrera con /config
                Engine engineSnapshot;
                String modeSnapshot;
                synchronized (modeLock) {
                    engineSnapshot = active.current;
                    modeSnapshot = active.mode;
                }

                engineSnapshot.accept(photo);

                String response = "{\"status\":\"ok\",\"mode\":\"" + modeSnapshot + "\"}";
                sendResponse(exchange, 200, response);

                System.out.println("[HTTP] add-task: " + dto.imageCode + " -> " + doctorIdStr + " (" + urgencyEnum + ") mode=" + modeSnapshot);

            } catch (Exception e) {
                e.printStackTrace();
                sendResponse(exchange, 500, "{\"error\":\"" + safeMsg(e.getMessage()) + "\"}");
            }
        }
    }

    /**
     * GET /status
     * Devuelve JSON estructurado:
     * {
     *   "mode": "monitor",
     *   "state": "...lo que devuelva engine.state()..."
     * }
     *
     * Ideal: si engine.state() ya devolviera JSON, aquí lo podrías devolver como objeto.
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

            // JSON estructurado (no string escapado)
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
     * NOTA: cambiar de modo NO migra el estado (cada engine mantiene sus colas).
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

                synchronized (modeLock) {
                    if ("mp".equalsIgnoreCase(config.mode)) {
                        active.current = mpEngine;
                        active.mode = "mp";
                    } else {
                        active.current = monitorEngine;
                        active.mode = "monitor";
                    }
                }

                System.out.println("[HTTP] config: mode changed to " + active.mode);
                sendResponse(exchange, 200, "{\"current_mode\":\"" + active.mode + "\",\"note\":\"state is not migrated\"}");

            } catch (Exception e) {
                e.printStackTrace();
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

    // =========================================================================
    // DTOs
    // =========================================================================

    static class TaskDto {
        String imageCode;
        int doctorId; // Node-RED envía 1,2,3...
        int urgency;  // Node-RED envía 1,2,3...
    }

    static class ConfigDto {
        String mode;
    }
}