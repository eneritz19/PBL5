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

class LoginServiceTest {

    private Connection conn;

    @BeforeEach
    void setup() throws Exception {
        conn = DriverManager.getConnection("jdbc:h2:mem:loginDb;DB_CLOSE_DELAY=-1");
        conn.createStatement().execute("CREATE TABLE users (id INT PRIMARY KEY AUTO_INCREMENT, email VARCHAR(255) UNIQUE, password_hash VARCHAR(255));");

        insertUser("user1@example.com", "password123");
    }

    @AfterEach
    void tearDown() throws Exception {
        conn.createStatement().execute("DROP ALL OBJECTS");
        conn.close();
    }

    private void insertUser(String email, String plainPassword) throws Exception {
        String hash = sha256(plainPassword);
        PreparedStatement ps = conn.prepareStatement("INSERT INTO users(email,password_hash) VALUES(?,?)");
        ps.setString(1, email);
        ps.setString(2, hash);
        ps.executeUpdate();
        ps.close();
    }

    private String sha256(String s) throws Exception {
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        byte[] h = md.digest(s.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : h) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private boolean attemptLogin(String email, String password) throws Exception {
        PreparedStatement ps = conn.prepareStatement("SELECT password_hash FROM users WHERE email = ?");
        ps.setString(1, email);
        ResultSet rs = ps.executeQuery();
        if (!rs.next()) return false;
        String stored = rs.getString(1);
        rs.close();
        ps.close();
        return stored.equals(sha256(password));
    }

    @Test
    void loginFailsWhenEmailNotFound() throws Exception {
        assertFalse(attemptLogin("noexiste@example.com", "whatever"));
    }

    @Test
    void loginFailsWithWrongPassword() throws Exception {
        assertFalse(attemptLogin("user1@example.com", "wrongpass"));
    }

    @Test
    void loginSucceedsWithCorrectCredentials() throws Exception {
        assertTrue(attemptLogin("user1@example.com", "password123"));
    }
}
