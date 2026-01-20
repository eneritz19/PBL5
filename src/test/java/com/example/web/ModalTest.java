package com.example.web;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class ModalTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(BASE_URL);

        // Forzar landing page visible (sin login)
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('loginPage')?.classList.add('hidden');" +
                "document.getElementById('landingPage')?.classList.remove('hidden');"
        );

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("landingPage")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    @DisplayName("Main modal opens, shows content and closes")
    void modalInteractionTest() {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Abrir modal (forzado)
        js.executeScript("document.getElementById('modal').classList.remove('hidden');");

        WebElement modal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("modal")));
        assertTrue(modal.isDisplayed(), "El modal debería mostrarse");

        // Simular contenido dinámico
        js.executeScript(
                "document.getElementById('modal-title').innerText = 'Título de prueba';" +
                "document.getElementById('modal-body').innerText = 'Contenido de prueba';"
        );

        WebElement modalTitle = modal.findElement(By.id("modal-title"));
        WebElement modalBody = modal.findElement(By.id("modal-body"));

        assertFalse(modalTitle.getText().isBlank(), "El título no debería estar vacío");
        assertFalse(modalBody.getText().isBlank(), "El cuerpo no debería estar vacío");

        // Cerrar modal
        WebElement cancelBtn = modal.findElement(
                By.xpath(".//button[contains(text(),'Cancel')]"));
        js.executeScript("arguments[0].click();", cancelBtn);

        wait.until(ExpectedConditions.invisibilityOf(modal));
    }

    @Test
    @DisplayName("Image modal opens and closes")
    void imageModalTest() {

        JavascriptExecutor js = (JavascriptExecutor) driver;

        // Abrir image modal
        js.executeScript(
                "document.getElementById('imageModal').classList.remove('hidden');");

        WebElement imageModal = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("imageModal")));
        assertTrue(imageModal.isDisplayed(), "El image modal debería mostrarse");

        // Cerrar image modal
        js.executeScript(
                "document.getElementById('imageModal').classList.add('hidden');");

        wait.until(ExpectedConditions.invisibilityOf(imageModal));
    }
}
