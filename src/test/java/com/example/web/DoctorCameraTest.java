package com.example.web;

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
    private static final String BASE_URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--use-fake-device-for-media-stream");
        options.addArguments("--use-fake-ui-for-media-stream");
        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get(BASE_URL);

        WebElement loginButtonMain = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Login')]")
        ));
        loginButtonMain.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("jperez@clinic.com");
        driver.findElement(By.id("password")).sendKeys("med123");
        driver.findElement(By.id("loginBtn")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        WebElement cameraTab = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[@onclick=\"openTab('d-camera', this)\"]")
                )
        );
        cameraTab.click();

        wait.until(ExpectedConditions.attributeContains(By.id("d-camera"), "class", "active"));

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

        btnCamera.click();

        wait.until(d -> video.isDisplayed());
        assertTrue(video.isDisplayed(), "Video should be visible when camera is started");
    }
}
