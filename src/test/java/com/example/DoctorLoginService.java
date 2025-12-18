package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import static org.junit.jupiter.api.Assertions.*;

class DoctorLoginServiceTest {

    private Connection conn;

    @BeforeEach
    void setup() throws Exception {
        // Conexión a base de datos en memoria
        conn = DriverManager.getConnection("jdbc:h2:mem:doctorDb;DB_CLOSE_DELAY=-1");

        // Creamos la tabla con tu estructura exacta
        conn.createStatement().execute(
                "CREATE TABLE doctors (" +
                        "doctor_code VARCHAR(20) PRIMARY KEY, " +
                        "name VARCHAR(255), " +
                        "email VARCHAR(255) UNIQUE, " +
                        "password_hash VARCHAR(255), " +
                        "id_clinic INT" +
                        ");");

        // Insertamos tus doctores reales para el test
        insertDoctor("MED001", "Dr. Juan Perez", "jperez@clinic.com", "med123", 1);
        insertDoctor("MED002", "Dra. Laura Gomez", "lgomez@clinic.com", "med123", 1);
        insertDoctor("MED003", "Dr. Carlos Ruiz", "cruiz@clinic.com", "med123", 2);
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.createStatement().execute("DROP ALL OBJECTS");
        conn.close();
    }

    private void insertDoctor(String code, String name, String email, String plainPassword, int clinicId)
            throws Exception {
        String hash = sha256(plainPassword);
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO doctors(doctor_code, name, email, password_hash, id_clinic) VALUES(?,?,?,?,?)");
        ps.setString(1, code);
        ps.setString(2, name);
        ps.setString(3, email);
        ps.setString(4, hash);
        ps.setInt(5, clinicId);
        ps.executeUpdate();
        ps.close();
    }

    private String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : h)
            sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private boolean attemptLogin(String email, String password) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT password_hash FROM doctors WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (!rs.next())
            return false;
        String stored = rs.getString(1);
        rs.close();
        ps.close();
        return stored.equals(sha256(password));
    }

    @Test
    void loginSucceedsForJuanPerez() throws Exception {
        assertTrue(attemptLogin("jperez@clinic.com", "med123"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        assertFalse(attemptLogin("cruiz@clinic.com", "passwordIncorrecto"));
    }

    @Test
    void checkDoctorCodeAndClinic() throws Exception {
        // Verificar que los datos adicionales se guardaron bien
        PreparedStatement ps = conn.prepareStatement("SELECT doctor_code, id_clinic FROM doctors WHERE email = ?");
        ps.setString(1, "cruiz@clinic.com");
        ResultSet rs = ps.executeQuery();

        assertTrue(rs.next());
        assertEquals("MED003", rs.getString("doctor_code"));
        assertEquals(2, rs.getInt("id_clinic"));

        rs.close();
        ps.close();
    }

    @Test
    void loginFailsWhenDoctorFieldsAreEmpty() throws Exception {
        assertFalse(attemptLogin(" ", " "), "The system should treat blank spaces as empty fields.");
    }

    @Test
    void loginFailsWithEmptyPasswordForJuanPerez() throws Exception {
        // Específicamente para un usuario que sí existe en el setup
        assertFalse(attemptLogin("jperez@clinic.com", ""), "Juan Perez should not be able to log in without a password.");
    }
}
