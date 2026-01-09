package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class LocalStoragePersistenceTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        // Configurar Chrome en modo headless para tests automáticos
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;
        driver.get(URL);

        // Limpiar localStorage antes de cada test
        js.executeScript("window.localStorage.clear();");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // =======================
    // TEST LOGIN DOCTOR
    // =======================
    @Test
    @DisplayName("Login Doctor saves token and doctor_id in localStorage")
    void testDoctorLocalStorage() {
        login("jperez@clinic.com", "med123");

        // Esperar dashboard del doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        // Esperar a que los datos se escriban en localStorage
        String token = (String) wait.until(d ->
                js.executeScript("return window.localStorage.getItem('token');"));
        String doctorId = (String) wait.until(d ->
                js.executeScript("return window.localStorage.getItem('doctor_id');"));

        assertNotNull(token, "El token no debe ser null");
        assertFalse(token.isBlank(), "El token no debe estar vacío");

        assertNotNull(doctorId, "El doctor_id no debe ser null");
        assertFalse(doctorId.isBlank(), "El doctor_id no debe estar vacío");
    }

    // =======================
    // TEST LOGIN PATIENT
    // =======================
    @Test
    @DisplayName("Login Patient saves token and patient_id in localStorage")
    void testPatientLocalStorage() {
        login("ana@example.com", "ana123");

        // Esperar dashboard del paciente
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));

        String token = (String) wait.until(d ->
                js.executeScript("return window.localStorage.getItem('token');"));
        String patientId = (String) wait.until(d ->
                js.executeScript("return window.localStorage.getItem('patient_id');"));

        assertNotNull(token, "El token no debe ser null");
        assertFalse(token.isBlank(), "El token no debe estar vacío");

        assertNotNull(patientId, "El patient_id no debe ser null");
        assertFalse(patientId.isBlank(), "El patient_id no debe estar vacío");
    }

    // =======================
    // MÉTODO AUXILIAR DE LOGIN
    // =======================
    private void login(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys(email);

        driver.findElement(By.id("password"))
                .sendKeys(password);

        driver.findElement(By.xpath("//button[text()='Login']")).click();
    }
}
