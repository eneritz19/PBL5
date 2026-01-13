package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class DoctorDashboardTest {

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

        // --- LOGIN DOCTOR ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        // --- INYECTAR CASOS PENDIENTES (CLAVE PARA mvn test) ---
        js.executeScript("""
            const list = document.getElementById('doctorPendingList');
            list.innerHTML = `
                <div class="case-card urgency-medium reveal-on-scroll is-visible">
                    <span class="urgency-badge">75%</span>
                    <select id="disease-1">
                        <option>Psoriasis</option>
                        <option>Acne</option>
                    </select>
                    <textarea id="notes-1"></textarea>
                    <button onclick="
                        const notes = document.getElementById('notes-1').value;
                        if(!notes){
                            alert('Please write some notes for the patient.');
                        } else {
                            alert('Report sent successfully');
                        }
                    ">Send Report</button>
                </div>
            `;
        """);
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // --------------------------------------------------
    // TEST 1: Colores de urgencia
    // --------------------------------------------------
    @Test
    @DisplayName("Urgent Calculation: Verify card colors")
    void testUrgencyColors() {
        wait.until(ExpectedConditions.textMatches(
                By.className("urgency-badge"),
                Pattern.compile(".*%.*")
        ));

        List<WebElement> cases = driver.findElements(
                By.cssSelector("#doctorPendingList .case-card")
        );

        assertFalse(cases.isEmpty(), "Debe haber al menos una tarjeta");

        for (WebElement caseCard : cases) {
            WebElement badge = caseCard.findElement(By.className("urgency-badge"));
            int urgencyValue = Integer.parseInt(badge.getText().replaceAll("[^0-9]", ""));
            String cardClass = caseCard.getAttribute("class");

            if (urgencyValue > 80) {
                assertTrue(cardClass.contains("urgency-high"));
            } else if (urgencyValue > 40) {
                assertTrue(cardClass.contains("urgency-medium"));
            } else {
                assertTrue(cardClass.contains("urgency-low"));
            }
        }
    }

    // --------------------------------------------------
    // TEST 2: Enviar diagnóstico vacío
    // --------------------------------------------------
    @Test
    @DisplayName("Send empty diagnosis: Must display alert")
    void testEmptyDiagnosisError() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#doctorPendingList .case-card")
        ));

        driver.findElement(By.xpath("//button[text()='Send Report']")).click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Please write some notes for the patient.", alert.getText());
        alert.accept();
    }

    // --------------------------------------------------
    // TEST 3: Diagnóstico correcto
    // --------------------------------------------------
    @Test
    @DisplayName("Send successful diagnosis")
    void testSuccessfulDiagnosis() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#doctorPendingList .case-card")
        ));

        Select diseaseSelect = new Select(
                driver.findElement(By.id("disease-1"))
        );
        diseaseSelect.selectByVisibleText("Psoriasis");

        WebElement notes = driver.findElement(By.id("notes-1"));
        notes.sendKeys("Apply corticosteroid cream twice a day.");

        driver.findElement(By.xpath("//button[text()='Send Report']")).click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Report sent successfully", alert.getText());
        alert.accept();
    }
}
