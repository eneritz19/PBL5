package com.example;

import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionTest {

    @Test
    void canConnectAndCreateTables() throws Exception {
        String url = "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1";
        try (Connection conn = DriverManager.getConnection(url)) {
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE patients (id INT PRIMARY KEY AUTO_INCREMENT, email VARCHAR(255) UNIQUE, password VARCHAR(255));");
                st.execute("INSERT INTO patients(email,password) VALUES('ana@example.com','hash1');");

                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM patients");
                rs.next();
                int count = rs.getInt(1);
                assertEquals(1, count, "Debe existir 1 paciente en la tabla");
            }
        }
    }
}
