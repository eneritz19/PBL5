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
import java.util.Date;

public class Main {

    // --- VARIABLES GLOBALES ---
    private static Engine monitorEngine;
    private static Engine mpEngine;
    private static ActiveEngine active;
    private static final Gson gson = new Gson();

    // Clase auxiliar para gestionar el cambio de modo en caliente
    static class ActiveEngine {
        volatile Engine current;
        volatile String mode = "monitor";
    }

    public static void main(String[] args) throws Exception {
        System.out.println("Iniciando SkinXpert Operating Service (HTTP)...");

        // 1. Configuración de salida (Mantenemos la consola para ver los logs en Docker)
        UpdateSink sink = new ConsoleUpdateSink();

        // 2. Inicialización de los dos Engines (Monitor y Message Passing)
        DoctorQueueManager monitorMgr = new DoctorQueueManager(20);
        monitorEngine = new MonitorEngine(monitorMgr, sink);

        MPDoctorQueueManager mpMgr = new MPDoctorQueueManager();
        mpEngine = new MessagePassingEngine(mpMgr, sink);

        // 3. Configurar motor por defecto
        active = new ActiveEngine();
        active.current = monitorEngine; // Arrancamos en modo Monitor

        // 4. Levantar Servidor HTTP en el puerto 8082
        int port = 8082;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // --- DEFINICIÓN DE ENDPOINTS (RUTAS) ---

        // Ruta A: Recibir tarea desde Node-RED
        // POST http://localhost:8082/add-task
        server.createContext("/add-task", new TaskHandler());

        // Ruta B: Ver estado actual de las colas
        // GET http://localhost:8082/status
        server.createContext("/status", new StatusHandler());

        // Ruta C: Cambiar configuración (Monitor <-> MP)
        // POST http://localhost:8082/config
        server.createContext("/config", new ConfigHandler());

        // Arrancar servidor
        server.setExecutor(null);
        server.start();

        System.out.println("Servidor escuchando en: http://localhost:" + port);
        System.out.println("Modo inicial: " + active.mode);
    }

    // =========================================================================
    // HANDLERS (Manejadores de las peticiones HTTP)
    // =========================================================================

    /**
     * Maneja la recepción de nuevas fotos/tareas desde Node-RED.
     * Espera un JSON como: { "imageCode": "img1", "doctorId": 1, "urgency": 3 }
     */
    static class TaskHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);

            if ("POST".equals(exchange.getRequestMethod())) {
                try {
                    InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                    BufferedReader br = new BufferedReader(isr);
                    StringBuilder jsonBuilder = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        jsonBuilder.append(line);
                    }

                    TaskDto dto = gson.fromJson(jsonBuilder.toString(), TaskDto.class);

                    // 1. Convertir ID de médico (int -> String)
                    String doctorIdStr = "D" + dto.doctorId;

                    // 2. Convertir la urgencia numérica (int -> Enum Urgency)
                    // Asumimos: 3=ALTO, 2=MEDIO, 1=BAJO
                    PhotoMsg.Urgency urgencyEnum;
                    if (dto.urgency >= 3) {
                        urgencyEnum = PhotoMsg.Urgency.ALTO;
                    } else if (dto.urgency == 2) {
                        urgencyEnum = PhotoMsg.Urgency.MEDIO;
                    } else {
                        urgencyEnum = PhotoMsg.Urgency.BAJO;
                    }

                    // 3. Crear el mensaje
                    // CORRECCIÓN: Quitamos la fecha y pasamos el Enum
                    PhotoMsg photo = new PhotoMsg(
                        dto.imageCode, 
                        doctorIdStr, 
                        urgencyEnum
                    );

                    active.current.accept(photo);

                    String response = "{\"status\":\"ok\", \"msg\":\"Procesado en modo " + active.mode + "\"}";
                    sendResponse(exchange, 200, response);

                    System.out.println("[Main] Tarea recibida: " + dto.imageCode + " -> " + doctorIdStr + " (" + urgencyEnum + ")");

                } catch (Exception e) {
                    e.printStackTrace();
                    sendResponse(exchange, 500, "{\"error\":\"" + e.getMessage() + "\"}");
                }
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    /**
     * Devuelve el estado actual de las colas (JSON string).
     */
    static class StatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("GET".equals(exchange.getRequestMethod())) {
                // Llamamos a tu método state()
                String currentState = active.current.state();
                
                // Aseguramos que sea JSON válido (si state() devuelve texto plano, lo envolvemos)
                // Si state() ya devuelve JSON, puedes enviar currentState directamente.
                String jsonResponse = gson.toJson(currentState); 
                
                sendResponse(exchange, 200, jsonResponse);
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    /**
     * Permite cambiar entre modo 'monitor' y 'mp' remotamente.
     */
    static class ConfigHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            addCorsHeaders(exchange);
            if ("POST".equals(exchange.getRequestMethod())) {
                InputStreamReader isr = new InputStreamReader(exchange.getRequestBody(), StandardCharsets.UTF_8);
                ConfigDto config = gson.fromJson(new BufferedReader(isr), ConfigDto.class);

                if ("mp".equalsIgnoreCase(config.mode)) {
                    active.current = mpEngine;
                    active.mode = "mp";
                } else {
                    active.current = monitorEngine;
                    active.mode = "monitor";
                }
                
                System.out.println("[Main] Cambio de modo a: " + active.mode);
                sendResponse(exchange, 200, "{\"current_mode\":\"" + active.mode + "\"}");
            } else {
                exchange.sendResponseHeaders(405, -1);
            }
        }
    }

    // --- UTILIDADES ---

    private static void sendResponse(HttpExchange exchange, int code, String response) throws IOException {
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    private static void addCorsHeaders(HttpExchange exchange) {
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type,Authorization");
    }

    // --- CLASES DE TRANSFERENCIA DE DATOS (DTOs) ---
    // Estas clases sirven para mapear el JSON que llega automáticamente

    static class TaskDto {
        String imageCode;
        int doctorId; // Node-RED envía 1, 2, 3...
        int urgency;  // Node-RED envía 1, 2, 3...
    }
    
    static class ConfigDto {
        String mode;
    }
}