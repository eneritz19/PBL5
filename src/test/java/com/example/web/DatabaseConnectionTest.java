package com.example.web;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @Test
    void canConnectAndCreatePatientsTable() throws Exception {

        String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";

        try (Connection conn = DriverManager.getConnection(url);
             Statement st = conn.createStatement()) {

            // Crear tabla patients (estructura coherente con tu proyecto)
            st.execute(
                    "CREATE TABLE patients (" +
                            "id INT PRIMARY KEY AUTO_INCREMENT, " +
                            "email VARCHAR(255) UNIQUE, " +
                            "password_hash VARCHAR(255)" +
                            ")"
            );

            // Insertar paciente de prueba
            st.execute(
                    "INSERT INTO patients(email, password_hash) " +
                            "VALUES ('ana@example.com', 'hash_prueba')"
            );

            // Verificar que se insertó correctamente
            ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patients");
            assertTrue(rs.next());

            int count = rs.getInt(1);
            assertEquals(1, count, "Debe existir exactamente 1 paciente en la tabla");
        }
    }
}
