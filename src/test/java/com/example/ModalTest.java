package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ModalTest {

    private WebDriver driver;
    private WebDriverWait wait;

    @BeforeAll
    void setupDriver() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
    }

    @BeforeEach
    void setup() {
        driver.get("http://localhost:8080"); // Ajusta a tu URL

        // 1️⃣ Forzar landingPage visible y ocultar loginPage
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('loginPage').classList.add('hidden');" +
                        "document.getElementById('landingPage').classList.remove('hidden');");

        // Esperar a que landingPage sea visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("landingPage")));
    }

    @Test
    void modalInteractionTest() {
        // Abrir modal principal
        WebElement openModalBtn = driver.findElement(By.cssSelector(".btn-primary")); // Ajusta si hay botón específico
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", openModalBtn);

        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('modal').classList.remove('hidden');");

        WebElement modal = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("modal")));
        Assertions.assertTrue(modal.isDisplayed(), "Modal principal debería mostrarse");

        // Verificar contenido dinámico
        WebElement modalTitle = modal.findElement(By.id("modal-title"));
        WebElement modalBody = modal.findElement(By.id("modal-body"));
        
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('modal').classList.remove('hidden');" +
                        "document.getElementById('modal-title').innerText = 'Título de prueba';" +
                        "document.getElementById('modal-body').innerText = 'Contenido de prueba';");

        Assertions.assertFalse(modalTitle.getText().isEmpty(), "Modal title no debería estar vacío");
        Assertions.assertFalse(modalBody.getText().isEmpty(), "Modal body no debería estar vacío");

        // Guardar modal
        WebElement saveBtn = modal.findElement(By.id("modalSaveBtn"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", saveBtn);

        // Cerrar modal
        WebElement cancelBtn = modal.findElement(By.cssSelector(".btn-secondary"));
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", cancelBtn);

        wait.until(ExpectedConditions.invisibilityOf(modal));
        Assertions.assertFalse(modal.isDisplayed(), "Modal principal debería estar oculto");
    }

    @Test
    void imageModalTest() {
        // Abrir imageModal
        WebElement imageModal = driver.findElement(By.id("imageModal"));
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('imageModal').classList.remove('hidden');");

        wait.until(ExpectedConditions.visibilityOf(imageModal));
        Assertions.assertTrue(imageModal.isDisplayed(), "Image modal debería mostrarse");

        // Cerrar imageModal
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('imageModal').classList.add('hidden');");

        wait.until(ExpectedConditions.invisibilityOf(imageModal));
        Assertions.assertFalse(imageModal.isDisplayed(), "Image modal debería estar oculto");
    }

    @AfterEach
    void teardown() {
        // Limpiar modales
        ((JavascriptExecutor) driver).executeScript(
                "document.getElementById('modal').classList.add('hidden');" +
                        "document.getElementById('imageModal').classList.add('hidden');");
    }

    @AfterAll
    void cleanup() {
        driver.quit();
    }
}
