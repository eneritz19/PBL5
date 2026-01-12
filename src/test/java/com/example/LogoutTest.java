package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Tag;

@Tag("ui")
class LogoutTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html"; 

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        driver.manage().window().maximize(); // opcional
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(URL);

        // Esperar a que pase el splash screen
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void testLogoutAsPatient() {
        // 1. Loguearse
        driver.findElement(By.id("email")).sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // 2. Esperar dashboard paciente
        WebElement dashboard = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));
        assertTrue(dashboard.isDisplayed(), "Deberíamos estar en el Dashboard");

        // 3. Pulsar Logout
        WebElement logoutBtn = driver.findElement(By.xpath("//button[text()='Logout']"));
        logoutBtn.click();

        // 4. Verificar regreso a login
        WebElement loginPage = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPage")));
        assertTrue(loginPage.isDisplayed(), "Tras el logout, la página de login debe ser visible");
        
        // Inputs vacíos
        assertEquals("", driver.findElement(By.id("email")).getAttribute("value"));
        assertEquals("", driver.findElement(By.id("password")).getAttribute("value"));
    }
}
