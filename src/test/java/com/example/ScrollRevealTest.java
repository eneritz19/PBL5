package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ScrollRevealTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);

        // Login como Doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("doctorDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Patients section has reveal-on-scroll card structure")
    void testRevealOnScrollStructureExists() {

        // Click por JS (headless-safe)
        WebElement patientsTab = wait.until(
                ExpectedConditions.presenceOfElementLocated(
                        By.xpath("//button[contains(text(),'Patients')]")
                )
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", patientsTab);

        // Verificar que el contenedor EXISTE (no visibilidad)
        WebElement container = wait.until(
                ExpectedConditions.presenceOfElementLocated(By.id("d-patients"))
        );
        assertNotNull(container, "El contenedor de pacientes no existe");

        // Buscar tarjetas con clase de animación
        List<WebElement> cards = driver.findElements(
                By.cssSelector("#d-patients .case-card.reveal-on-scroll")
        );

        assertFalse(cards.isEmpty(),
                "No existen tarjetas con la clase reveal-on-scroll en Patients");
    }
}
