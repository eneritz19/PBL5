package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

class ServerErrorTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");
        options.addArguments("--window-size=1920,1080");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(URL);

        // Esperar a que el login esté visible
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPage")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Login does not proceed when backend is unavailable")
    void loginFailsWhenServerIsDown() throws InterruptedException {

        driver.findElement(By.id("email")).sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // Esperar a que falle el fetch
        Thread.sleep(1500);

        // SEGUIMOS en el login
        WebElement loginPage = driver.findElement(By.id("loginPage"));
        assertTrue(
                loginPage.isDisplayed(),
                "El usuario debería permanecer en el login si el servidor está caído"
        );
    }
}
