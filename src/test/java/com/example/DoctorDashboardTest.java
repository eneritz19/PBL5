package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class DoctorDashboardTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);

        // Login doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    private void acceptAlertIfPresent() {
        try {
            WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(2));
            Alert alert = shortWait.until(ExpectedConditions.alertIsPresent());
            alert.accept();
        } catch (TimeoutException ignored) {
            // No hay alert → seguimos
        }
    }

    @Test
    @DisplayName("Urgent Calculation: Verify card colors")
    void testUrgencyColors() {
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("doctorPendingList")));
        wait.until(ExpectedConditions.textMatches(
                By.className("urgency-badge"),
                java.util.regex.Pattern.compile(".*%.*")));

        List<WebElement> cases = driver.findElements(By.cssSelector("#doctorPendingList .case-card"));
        for (WebElement caseCard : cases) {
            WebElement badge = caseCard.findElement(By.className("urgency-badge"));
            String numericText = badge.getText().replaceAll("[^0-9]", "");
            if (numericText.isEmpty()) continue;

            int urgencyValue = Integer.parseInt(numericText);
            String cardClass = caseCard.getAttribute("class");

            if (urgencyValue > 80) assertTrue(cardClass.contains("urgency-high"));
            else if (urgencyValue > 40) assertTrue(cardClass.contains("urgency-medium"));
            else assertTrue(cardClass.contains("urgency-low"));
        }
    }

    @Test
    @DisplayName("Send empty diagnosis: report is NOT sent")
    void testEmptyDiagnosisError() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#doctorPendingList .case-card")));

        driver.findElement(By.xpath("//button[text()='Send Report']")).click();

        // Capturamos y cerramos el alert
        acceptAlertIfPresent();

        // Verificamos que el caso sigue presente (no se envió)
        List<WebElement> casesAfter = driver.findElements(By.cssSelector("#doctorPendingList .case-card"));
        assertFalse(casesAfter.isEmpty(), "Case should not be removed when diagnosis is empty");
    }

    @Test
    @DisplayName("Send successful diagnosis removes case from list")
    void testSuccessfulDiagnosis() {
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.cssSelector("#doctorPendingList .case-card")));

        List<WebElement> casesBefore = driver.findElements(By.cssSelector("#doctorPendingList .case-card"));

        WebElement selectElement = driver.findElement(By.cssSelector("select[id^='disease-']"));
        new Select(selectElement).selectByVisibleText("Psoriasis");

        WebElement notes = driver.findElement(By.cssSelector("textarea[id^='notes-']"));
        notes.sendKeys("Treatment with corticosteroid cream twice a day.");

        driver.findElement(By.xpath("//button[text()='Send Report']")).click();

        // Capturamos y cerramos el alert de éxito
        acceptAlertIfPresent();

        // Esperamos que la lista tenga menos elementos → caso enviado
        wait.until(d ->
                driver.findElements(By.cssSelector("#doctorPendingList .case-card")).size() < casesBefore.size());
    }
}
