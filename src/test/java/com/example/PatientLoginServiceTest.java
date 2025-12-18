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

class PatientLoginServiceTest {

    private Connection conn;

    @BeforeEach
    void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:patientDb;DB_CLOSE_DELAY=-1");
        conn.createStatement().execute(
                "CREATE TABLE patients (dni VARCHAR(20), name VARCHAR(255), email VARCHAR(255), password_hash VARCHAR(255), id_doctor INT);");

        // Insertar tus datos reales en la base de datos de prueba
        insertPatient("ana@example.com", "ana123", "Ana Martinez");
        insertPatient("pedro@example.com", "pedro123", "Pedro Lopez");
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.createStatement().execute("DROP ALL OBJECTS");
        conn.close();
    }

    private void insertPatient(String email, String plainPassword, String name) throws Exception {
        String hash = sha256(plainPassword);
        PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO patients(email,password_hash,name) VALUES(?,?,?)");
        ps.setString(1, email);
        ps.setString(2, hash);
        ps.setString(3, name);
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
                "SELECT password_hash FROM patients WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (!rs.next())
            return false;
        String stored = rs.getString(1);
        rs.close();
        ps.close();
        return stored.equals(sha256(password));
    }

    private String getPatientName(String email) throws Exception {
        PreparedStatement ps = conn.prepareStatement(
                "SELECT name FROM patients WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (!rs.next())
            return null;
        String name = rs.getString(1);
        rs.close();
        ps.close();
        return name;
    }

    @Test
    void loginFailsWhenEmailNotFound() throws Exception {
        assertFalse(attemptLogin("noexiste@example.com", "ana123"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        assertFalse(attemptLogin("ana@example.com", "wrongpass"));
    }

    @Test
    void loginSucceedsWithCorrectCredentials() throws Exception {
        assertTrue(attemptLogin("ana@example.com", "ana123"));
        assertEquals("Ana Martinez", getPatientName("ana@example.com"));
    }

    @Test
    void loginFailsWhenEmailIsEmpty() throws Exception {
        assertFalse(attemptLogin("", "ana123"), "Login should not be allowed with an empty email address.");
    }

    @Test
    void loginFailsWhenPasswordIsEmpty() throws Exception {
        assertFalse(attemptLogin("ana@example.com", ""), "Login should not be allowed with an empty password");
    }

    @Test
    void loginFailsWhenBothFieldsAreEmpty() throws Exception {
        assertFalse(attemptLogin("", ""), "The login should fail if all fields are empty.");
    }
}
