package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class LogoutTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html"; 

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);
        // Esperar a que pase el splash screen
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testLogoutAsPatient() {
        // 1. Primero necesitamos estar logueados (asegúrate de que Node-RED esté ON para este paso)
        driver.findElement(By.id("email")).sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // 2. Esperar a que el Dashboard sea visible
        WebElement dashboard = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));
        assertTrue(dashboard.isDisplayed(), "Deberíamos estar en el Dashboard");

        // 3. Buscar y pulsar el botón de Logout (en tu HTML dice "Logout" para pacientes)
        WebElement logoutBtn = driver.findElement(By.xpath("//button[text()='Logout']"));
        logoutBtn.click();

        // 4. VERIFICACIÓN: Al recargar, el loginPage debe volver a ser visible y el dashboard no
        // Esperamos a que el ID 'loginPage' sea visible de nuevo
        WebElement loginPage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPage")));
        
        assertTrue(loginPage.isDisplayed(), "Tras el logout, la página de login debe ser visible");
        
        // Opcionalmente verificar que los inputs están vacíos tras la recarga
        assertEquals("", driver.findElement(By.id("email")).getAttribute("value"));
    }
}
