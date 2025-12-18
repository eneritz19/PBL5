package test.java.com.example;

import org.junit.jupiter.api.*;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PatientPanelServiceTest {

    private Connection conn;

    /* ======================================================
       SETUP / TEARDOWN
       ====================================================== */

    @BeforeEach
    void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:skinXpert;DB_CLOSE_DELAY=-1");

        // TABLAS MINIMAS PARA EL PANEL PACIENTE
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

        // DATOS BASE
        conn.createStatement().execute("INSERT INTO patients(name) VALUES ('Ana Martinez');");
        conn.createStatement().execute("INSERT INTO doctors(name) VALUES ('Dr. Juan Perez');");
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.createStatement().execute("DROP ALL OBJECTS");
        conn.close();
    }

    /* ======================================================
       SERVICIOS SIMULADOS (LO QUE CONSUME EL FRONT)
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
       TESTS – PANEL DEL PACIENTE
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
}