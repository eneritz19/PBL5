package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.*;

@Tag("ui")
class ScrollRevealTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get(URL);

        // Login como Doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // Esperar dashboard doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Reveal-on-scroll animation is applied when patient cards exist")
    void testRevealOnScrollPatients() {

        // Abrir pestaña Patients
        WebElement patientsTab = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//div[@id='doctorDashboard']//button[contains(text(),'Patients')]")
                )
        );
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", patientsTab);

        // Esperar a que la pestaña esté activa
        wait.until(ExpectedConditions.attributeContains(
                By.id("d-patients"), "class", "active"
        ));

        // Buscar tarjetas (sin wait, pueden no existir)
        List<WebElement> cards = driver.findElements(
                By.cssSelector("#d-patients .case-card.reveal-on-scroll")
        );

        // Si no hay pacientes, el test se marca como válido
        assumeTrue(!cards.isEmpty(), "No hay pacientes asignados al doctor");

        // Primera tarjeta debe ser visible
        WebElement firstCard = cards.get(0);
        assertTrue(
                firstCard.getAttribute("class").contains("is-visible"),
                "La primera tarjeta no tiene la clase 'is-visible'"
        );

        // Si hay más tarjetas, probar scroll
        if (cards.size() > 1) {
            WebElement lastCard = cards.get(cards.size() - 1);

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior:'instant', block:'center'});",
                    lastCard
            );

            wait.until(d ->
                    lastCard.getAttribute("class").contains("is-visible")
            );

            assertTrue(
                    lastCard.getAttribute("class").contains("is-visible"),
                    "La animación reveal-on-scroll no se activó en la última tarjeta"
            );
        }
    }
}
