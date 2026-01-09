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
class DoctorCameraUnavailableTest {

        private WebDriver driver;
        private WebDriverWait wait;
        private final String URL = "http://localhost:8080/index.html";

        @BeforeEach
        void setup() {

                // Chrome headless SIN camara falsa
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

                driver.findElement(By.id("password"))
                                .sendKeys("med123");

                driver.findElement(By.xpath("//button[text()='Login']")).click();

                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

                // Ir a Camara
                WebElement cameraTab = wait.until(
                                ExpectedConditions.elementToBeClickable(
                                                By.xpath("//button[contains(text(),'Camera')]")));
                cameraTab.click();
        }

        @AfterEach
        void tearDown() {
                if (driver != null) {
                        driver.quit();
                }
        }

        // Camara NO disponible

        @Test
        @DisplayName("Camera not available: video does not start")
        void testCameraNotAvailable() {

                WebElement btnCamera = wait.until(
                                ExpectedConditions.elementToBeClickable(By.id("btnCamera")));

                WebElement video = driver.findElement(By.id("camera-preview"));

                btnCamera.click();

                // Esperamos un poco a que el JS intente iniciar la cámara
                try {
                        Thread.sleep(1000);
                } catch (InterruptedException ignored) {
                }

                // La cámara NO debe mostrarse
                assertEquals(
                                "none",
                                video.getCssValue("display"),
                                "Video should remain hidden when camera is unavailable");
        }

}
