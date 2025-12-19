package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
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
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);

        // Login como Doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null)
            driver.quit();
    }

    // TEST UX: Reveal on scroll en lista pacientes

    @Test
    @DisplayName("Reveal-on-scroll animation is applied to cards when scrolling")
    void testRevealOnScrollPatients() {

        // Ir a pestaña Patients
        WebElement patientsTab = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Patients')]")));
        patientsTab.click();

        // Esperar tarjetas
        List<WebElement> cards = wait.until(
                ExpectedConditions.presenceOfAllElementsLocatedBy(
                        By.cssSelector("#d-patients .case-card.reveal-on-scroll")));

        assertFalse(cards.isEmpty(), "There are no cards for testing");

        // Verificar que la primera tarjeta YA tiene el efecto
        WebElement firstCard = cards.get(0);
        assertTrue(
                firstCard.getAttribute("class").contains("is-visible"),
                "The visible card does not have the is-visible class");

        // Si hay mas tarjetas, forzar scroll a la última
        if (cards.size() > 1) {
            WebElement lastCard = cards.get(cards.size() - 1);

            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({behavior:'instant', block:'center'});",
                    lastCard);

            // Esperar a que tambien tenga is-visible
            wait.until(driver -> lastCard.getAttribute("class").contains("is-visible"));

            assertTrue(
                    lastCard.getAttribute("class").contains("is-visible"),
                    "The reveal-on-scroll animation did not activate on lower cards");
        }
    }

}
