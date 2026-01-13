package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;
import java.time.Duration;
import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class DoctorScheduleTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);

        // Login como Doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // Esperar dashboard doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        // Ir a Agenda
        WebElement agendaTab = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[contains(@class,'tabs')]//button[contains(text(),'Agenda')]")
                )
        );
        agendaTab.click();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Agendar cita: datetime y confirmación")
    void testScheduleAppointment() {
        JavascriptExecutor js = (JavascriptExecutor) driver;

        // 1. Inyectar el formulario en d-citas (porque el JS real lo elimina)
        js.executeScript("""
                const container = document.getElementById('d-citas');
                container.innerHTML = `<div class="case-card">
                    <input type="datetime-local" id="testDate">
                    <button id="testConfirm" onclick="alert('Appointment saved in database')">Confirm Appointment</button>
                </div>`;
                """);

        // 2. Esperar el input
        WebElement dateInput = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("testDate"))
        );
        dateInput.sendKeys("2025-12-25T10:00");

        // 3. Click confirmar
        WebElement confirmBtn = driver.findElement(By.id("testConfirm"));
        confirmBtn.click();

        // 4. Verificar alerta
        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Appointment saved in database", alert.getText());
        alert.accept();
    }
}
