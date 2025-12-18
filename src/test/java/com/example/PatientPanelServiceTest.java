package test.java.com.example;

import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import org.junit.jupiter.api.*;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientPanelServiceTest {

    private Connection conn;
    private static MockWebServer mockServer;
    private static String BASE_URL;
    private static HttpClient client;

    /* ======================================================
       MOCK SERVER SETUP
       ====================================================== */

    @BeforeAll
    static void setupServer() throws Exception {
        mockServer = new MockWebServer();
        mockServer.start();
        BASE_URL = mockServer.url("/").toString();
        client = HttpClient.newHttpClient();
    }

    @AfterAll
    static void shutdownServer() throws Exception {
        mockServer.shutdown();
    }

    /* ======================================================
       DATABASE SETUP / TEARDOWN
       ====================================================== */

    @BeforeEach
    void setupDB() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:skinXpert;DB_CLOSE_DELAY=-1");

        conn.createStatement().execute("""
            CREATE TABLE patients (
                id_patient INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(150)
            );
        """);

        conn.createStatement().execute("""
            CREATE TABLE doctors (
                id_doctor INT AUTO_INCREMENT PRIMARY KEY,
                name VARCHAR(150)
            );
        """);

        conn.createStatement().execute("""
            CREATE TABLE appointments (
                id_appointment INT AUTO_INCREMENT PRIMARY KEY,
                id_patient INT,
                id_doctor INT,
                date TIMESTAMP,
                status VARCHAR(20)
            );
        """);

        conn.createStatement().execute("""
            CREATE TABLE diagnoses (
                id_diagnosis INT AUTO_INCREMENT PRIMARY KEY,
                id_patient INT,
                diagnosis_date TIMESTAMP,
                disease VARCHAR(255),
                doctor_notes TEXT
            );
        """);

        conn.createStatement().execute("INSERT INTO patients(name) VALUES ('Ana Martinez');");
        conn.createStatement().execute("INSERT INTO doctors(name) VALUES ('Dr. Juan Perez');");
    }

    @AfterEach
    void tearDownDB() throws Exception {
        conn.createStatement().execute("DROP ALL OBJECTS");
        conn.close();
    }

    /* ======================================================
       UTILS - HISTORIAL Y CITAS
       ====================================================== */

    private List<String> getPatientHistory(int patientId) throws Exception {
        PreparedStatement ps = conn.prepareStatement("""
            SELECT disease, doctor_notes FROM diagnoses
            WHERE id_patient = ?
        """);
        ps.setInt(1, patientId);

        ResultSet rs = ps.executeQuery();
        List<String> result = new ArrayList<>();

        while (rs.next()) {
            result.add(rs.getString("disease") + " - " + rs.getString("doctor_notes"));
        }

        rs.close();
        ps.close();
        return result;
    }

    private List<String> getPatientAppointments(int patientId) throws Exception {
        PreparedStatement ps = conn.prepareStatement("""
            SELECT d.name, a.status
            FROM appointments a
            JOIN doctors d ON a.id_doctor = d.id_doctor
            WHERE a.id_patient = ?
        """);
        ps.setInt(1, patientId);

        ResultSet rs = ps.executeQuery();
        List<String> result = new ArrayList<>();

        while (rs.next()) {
            result.add(rs.getString(1) + " - " + rs.getString(2));
        }

        rs.close();
        ps.close();
        return result;
    }

    /* ======================================================
       TESTS - PANEL DEL PACIENTE (H2 DB)
       ====================================================== */

    @Test
    void historyIsEmptyWhenPatientHasNoDiagnoses() throws Exception {
        List<String> history = getPatientHistory(1);
        assertTrue(history.isEmpty(), "El historial debería estar vacío");
    }

    @Test
    void appointmentsIsEmptyWhenPatientHasNoAppointments() throws Exception {
        List<String> appointments = getPatientAppointments(1);
        assertTrue(appointments.isEmpty(), "El paciente no debería tener citas");
    }

    @Test
    void historyLoadsCorrectlyWhenDiagnosisExists() throws Exception {
        PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO diagnoses(id_patient, diagnosis_date, disease, doctor_notes)
            VALUES (?, ?, ?, ?)
        """);
        ps.setInt(1, 1);
        ps.setTimestamp(2, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(3, "Acne and Rosacea");
        ps.setString(4, "Lesión leve, tratamiento tópico");
        ps.executeUpdate();
        ps.close();

        List<String> history = getPatientHistory(1);

        assertEquals(1, history.size());
        assertTrue(history.get(0).contains("Acne and Rosacea"));
        assertTrue(history.get(0).contains("Lesión leve"));
    }

    @Test
    void appointmentsLoadCorrectlyWhenAppointmentExists() throws Exception {
        PreparedStatement ps = conn.prepareStatement("""
            INSERT INTO appointments(id_patient, id_doctor, date, status)
            VALUES (?, ?, ?, ?)
        """);
        ps.setInt(1, 1);
        ps.setInt(2, 1);
        ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
        ps.setString(4, "pendiente");
        ps.executeUpdate();
        ps.close();

        List<String> appointments = getPatientAppointments(1);

        assertEquals(1, appointments.size());
        assertTrue(appointments.get(0).contains("Dr. Juan Perez"));
        assertTrue(appointments.get(0).contains("pendiente"));
    }

    /* ======================================================
       TESTS - MOCKEANDO API (HTTP)
       ====================================================== */

    @Test
    void inquiryFailsWhenNoImageProvided() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(400).setBody("{\"error\":\"No image\"}"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "patient/inquiry"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {
                      "patientId": 1,
                      "description": "Lesión sospechosa"
                    }
                """))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("No image"));
    }

    @Test
    void inquirySucceedsWhenImageIsProvided() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"success\":true}"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "patient/inquiry"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {
                      "patientId": 1,
                      "description": "Lesión sospechosa",
                      "image": "BASE64_IMAGE_DATA"
                    }
                """))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("success"));
    }

    @Test
    void historyEndpointReturnsData() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"disease\":\"Acne\",\"notes\":\"leve\"}]"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "patient/history?patientId=1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Acne"));
    }

    @Test
    void appointmentsEndpointReturnsData() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("[{\"doctor\":\"Dr. Juan Perez\",\"status\":\"pendiente\"}]"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "patient/appointments?patientId=1"))
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("Dr. Juan Perez"));
    }

    @Test
    void diagnosisFailsWhenNotesAreEmpty() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(400).setBody("{\"error\":\"Notes empty\"}"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "doctor/diagnosis"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {
                      "patientId": 1,
                      "notes": ""
                    }
                """))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(400, response.statusCode());
        assertTrue(response.body().contains("Notes empty"));
    }

    @Test
    void diagnosisIsSavedSuccessfully() throws Exception {
        mockServer.enqueue(new MockResponse().setResponseCode(200).setBody("{\"success\":true}"));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "doctor/diagnosis"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString("""
                    {
                      "patientId": 1,
                      "notes": "Lesión benigna, seguimiento en 2 semanas"
                    }
                """))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());
        assertTrue(response.body().contains("success"));
    }
}
