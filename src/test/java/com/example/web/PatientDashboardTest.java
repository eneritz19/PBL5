package com.example.web;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class PatientDashboardTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // --- Abrir página inicial ---
        driver.get(BASE_URL);

        // --- Click en Login en la primera pantalla ---
        WebElement loginButtonMain = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Login')]")
        ));
        loginButtonMain.click();

        // --- Login PACIENTE ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");
        driver.findElement(By.id("loginBtn")).click();

        // --- Esperar dashboard paciente cargado ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    // --------------------------------------------------
    // TEST 1: Verificar que el historial del paciente está visible
    // --------------------------------------------------
    @Test
    @DisplayName("Patient dashboard: History visible")
    void testHistoryIsVisible() {
        // Hacer click en la pestaña Historial
        WebElement historyTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'History')]")
        ));
        historyTab.click();

        // Esperar que la lista de historial esté visible en DOM
        WebElement historyList = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("patientHistoryList") // <-- asegúrate que este id coincide con tu HTML
        ));

        // Comprobar que está visible
        assertTrue(historyList.isDisplayed(), "El historial debería estar visible");
    }

    // --------------------------------------------------
    // TEST 2: Verificar que las citas del paciente están visibles
    // --------------------------------------------------
    @Test
    @DisplayName("Patient dashboard: Appointments visible")
    void testAppointmentsAreVisible() {
        // Hacer click en la pestaña Citas
        WebElement appointmentsTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Appointments')]")
        ));
        appointmentsTab.click();

        // Esperar que la lista de citas esté visible en DOM
        WebElement appointmentsList = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("patientAppointmentsList") // <-- asegúrate que este id coincide con tu HTML
        ));

        // Comprobar que está visible
        assertTrue(appointmentsList.isDisplayed(), "Las citas deberían estar visibles");
    }
}
