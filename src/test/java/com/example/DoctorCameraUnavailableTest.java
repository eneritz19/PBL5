package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class DoctorCameraUnavailableTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {

        // SIN fake camera
        driver = new ChromeDriver();
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
                        By.xpath("//button[contains(text(),'Camera')]")
                )
        );
        cameraTab.click();
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    // Camara NO disponible

    @Test
    @DisplayName("Camera not available displays alert")
    void testCameraNotAvailable() {

        WebElement btnCamera = wait.until(
                ExpectedConditions.elementToBeClickable(By.id("btnCamera"))
        );

        btnCamera.click();

        Alert alert = wait.until(ExpectedConditions.alertIsPresent());
        assertEquals("Camera not available", alert.getText());

        alert.accept();
    }
}
