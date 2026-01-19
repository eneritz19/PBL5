package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class DoctorCameraTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        // Chrome con cámara falsa y permisos automáticos
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--use-fake-device-for-media-stream");
        options.addArguments("--use-fake-ui-for-media-stream");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        // --- Abrir página inicial ---
        driver.get(URL);

        // --- Click en Login ---
        WebElement loginButtonMain = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Login')]")
        ));
        loginButtonMain.click();

        // --- Login DOCTOR ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.id("loginBtn")).click();

        // --- Esperar dashboard doctor cargado ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        // --- Ir a pestaña Cámara ---
        WebElement cameraTab = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@onclick=\"openTab('d-camera', this)\"]")
                )
        );
        cameraTab.click();

        // --- Esperar que el tab de cámara esté activo ---
        wait.until(ExpectedConditions.attributeContains(By.id("d-camera"), "class", "active"));

        // --- Esperar video y botón ---
        wait.until(ExpectedConditions.presenceOfElementLocated(By.id("camera-preview")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("btnCamera")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Camera loads correctly for doctor")
    void testCameraIsVisible() {
        WebElement btnCamera = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnCamera")));
        WebElement video = driver.findElement(By.id("camera-preview"));

        // Iniciar cámara
        btnCamera.click();

        // Verificar que el video se muestra
        wait.until(driver -> video.isDisplayed());
        assertTrue(video.isDisplayed(), "Video should be visible when camera is started");
    }
}
