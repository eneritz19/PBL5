package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class LocalStoragePersistenceTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;
        driver.get(URL);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    // TEST DOCTOR
    @Test
    @DisplayName("Login Doctor saves token and doctor_id in localStorage")
    void testDoctorLocalStorage() {
        login("jperez@clinic.com", "med123");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        String token = (String) js.executeScript("return window.localStorage.getItem('token');");
        String doctorId = (String) js.executeScript("return window.localStorage.getItem('doctor_id');");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertNotNull(doctorId);
        assertFalse(doctorId.isBlank());
    }

    // TEST PATIENT
    @Test
    @DisplayName("Login Patient saves token and patient_id in localStorage")
    void testPatientLocalStorage() {
        login("ana@example.com", "ana123");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));

        String token = (String) js.executeScript("return window.localStorage.getItem('token');");
        String patientId = (String) js.executeScript("return window.localStorage.getItem('patient_id');");

        assertNotNull(token);
        assertFalse(token.isBlank());
        assertNotNull(patientId);
        assertFalse(patientId.isBlank());
    }

    // METODO AUXILIAR
    private void login(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys(email);
        driver.findElement(By.id("password")).sendKeys(password);
        driver.findElement(By.xpath("//button[text()='Login']")).click();
    }
}
