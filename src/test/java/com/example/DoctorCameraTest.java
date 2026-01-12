package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Tag;

@Tag("ui")
class DoctorCameraTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {

        // Chrome headless + camara falsa + permisos automaticos
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--use-fake-device-for-media-stream");
        options.addArguments("--use-fake-ui-for-media-stream");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);

        // Login como Doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("jperez@clinic.com");

        driver.findElement(By.id("password"))
                .sendKeys("med123");

        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // Esperar dashboard doctor
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        // Ir a pestaña Camara
        WebElement cameraTab = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Camera')]")
                )
        );
        cameraTab.click();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // TEST 1: Start + Detener camara

    @Test
    @DisplayName("Camera: Start and stop correctly")
    void testStartAndStopCamera() {

        WebElement btnCamera = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("btnCamera"))
        );

        WebElement video = driver.findElement(By.id("camera-preview"));

        // Iniciar camara
        btnCamera.click();

        // Boton cambia a Detener
        wait.until(ExpectedConditions.textToBePresentInElement(btnCamera, "Detener"));

        // Video visible
        wait.until(d -> video.isDisplayed());

        // Detener camara
        btnCamera.click();

        // Boton vuelve a Iniciar
        wait.until(ExpectedConditions.textToBePresentInElement(btnCamera, "Iniciar"));

        // Video oculto
        assertEquals("none", video.getCssValue("display"));
    }

    // TEST 2: Camara disponible (permiso implicito)

    @Test
    @DisplayName("Camera available: no alert displayed")
    void testCameraAvailable() {

        WebElement btnCamera = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("btnCamera"))
        );

        btnCamera.click();

        // Esperamos un pequeño tiempo y verificamos que NO haya alert
        WebDriverWait shortWait = new WebDriverWait(driver, Duration.ofSeconds(3));

        assertThrows(TimeoutException.class, () ->
                shortWait.until(ExpectedConditions.alertIsPresent())
        );
    }
}
