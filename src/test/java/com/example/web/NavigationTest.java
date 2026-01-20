package com.example.web;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class NavigationTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private static final String BASE_URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        js = (JavascriptExecutor) driver;

        driver.get(BASE_URL);

        // ---- PASAR SPLASH / LOGIN ----
        WebElement loginMain = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Login')]"))
        );
        loginMain.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");
        driver.findElement(By.id("loginBtn")).click();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Test navigation through 'How it Works' steps safely")
    void testNavigationHowItWorks() {

        // 1️⃣ Esperar a que el landing page se muestre
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("landingPage")));
        js.executeScript("document.getElementById('landingPage').classList.remove('hidden');");

        // 2️⃣ Esperar a que el loader desaparezca
        wait.until(ExpectedConditions.invisibilityOfElementLocated(By.id("logoLoader")));

        // 3️⃣ Scroll hasta "How it Works"
        WebElement howItWorks = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("como-funciona")));
        js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", howItWorks);

        // 4️⃣ Interactuar con los steps
        List<WebElement> steps = driver.findElements(By.cssSelector(".step-indicator"));

        for (int i = 0; i < steps.size(); i++) {
            WebElement step = steps.get(i);

            // Scroll seguro antes de click
            js.executeScript("arguments[0].scrollIntoView({behavior: 'smooth', block: 'center'});", step);

            // Esperar a que no haya overlay y que sea clicable
            wait.until(ExpectedConditions.and(
                    ExpectedConditions.elementToBeClickable(step),
                    ExpectedConditions.invisibilityOfElementLocated(By.id("logoLoader"))
            ));

            // Click seguro
            js.executeScript("arguments[0].click();", step); // Usar JS para evitar bloqueos de overlay

            // Validar que se activó
            int index = i;
            wait.until(d -> steps.get(index).getAttribute("class").contains("active"));
            assertTrue(step.getAttribute("class").contains("active"),
                    "Step " + (i + 1) + " did not become active");
        }
    }
}
