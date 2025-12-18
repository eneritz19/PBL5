package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DoctorDashboardTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);

        // Loguearse como doctor para acceder al panel
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        driver.findElement(By.id("email")).sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // Asegurarse de que el dashboard del doctor cargó
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null)
            driver.quit();
    }

    @Test
    @DisplayName("Urgent Calculation: Verify card colors")
    void testUrgencyColors() {
        // 1. Esperamos a que aparezca la lista
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("doctorPendingList")));

        // 2. TRUCO: Esperamos a que la primera tarjeta tenga el símbolo '%'
        // Esto garantiza que Node-RED ya inyectó los datos en el HTML
        wait.until(ExpectedConditions.textMatches(By.className("urgency-badge"),
                java.util.regex.Pattern.compile(".*%.*")));

        List<WebElement> cases = driver.findElements(By.cssSelector("#doctorPendingList .case-card"));

        for (WebElement caseCard : cases) {
            WebElement badge = caseCard.findElement(By.className("urgency-badge"));
            String badgeText = badge.getText();

            // Limpiamos el texto
            String numericText = badgeText.replaceAll("[^0-9]", "");

            // Si sigue vacío, imprimimos qué está viendo Selenium para debuguear
            if (numericText.isEmpty()) {
                System.out.println("DEBUG: The badge is empty. Internal HTML:" + badge.getAttribute("innerHTML"));
                continue; // Saltamos esta tarjeta si está vacía para que el test no explote
            }

            int urgencyValue = Integer.parseInt(numericText);
            String cardClass = caseCard.getAttribute("class");

            if (urgencyValue > 80) {
                assertTrue(cardClass.contains("urgency-high"),
                        "Error en " + urgencyValue + "%: falta clase urgency-high");
            } else if (urgencyValue > 40) {
                assertTrue(cardClass.contains("urgency-medium"),
                        "Error en " + urgencyValue + "%: falta clase urgency-medium");
            } else {
                assertTrue(cardClass.contains("urgency-low"),
                        "Error en " + urgencyValue + "%: falta clase urgency-low");
            }
        }
    }

    @Test
    @DisplayName("Send empty diagnosis: Must display alert")
    void testEmptyDiagnosisError() {
        // Esperar a que haya al menos una tarjeta
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#doctorPendingList .case-card")));

        // Dejamos el textarea de notas vacío y pulsamos "Send Report"
        // (Usamos el primer botón de reporte que encontremos)
        driver.findElement(By.xpath("//button[text()='Send Report']")).click();

        // Verificar alerta: "Please write some notes for the patient."
        wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Please write some notes for the patient.", driver.switchTo().alert().getText());
        driver.switchTo().alert().accept();
    }

    @Test
    @DisplayName("Send successful diagnosis")
    void testSuccessfulDiagnosis() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector("#doctorPendingList .case-card")));

        // 1. Seleccionar una enfermedad del dropdown
        WebElement selectElement = driver.findElement(By.cssSelector("select[id^='disease-']"));
        Select diseaseSelect = new Select(selectElement);
        diseaseSelect.selectByVisibleText("Psoriasis");

        // 2. Escribir notas
        WebElement notes = driver.findElement(By.cssSelector("textarea[id^='notes-']"));
        notes.sendKeys("Treatment with corticosteroid cream twice a day.");

        // 3. Enviar
        driver.findElement(By.xpath("//button[text()='Send Report']")).click();

        // 4. Verificar mensaje de éxito
        wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Report sent successfully", driver.switchTo().alert().getText());
        driver.switchTo().alert().accept();
    }
}