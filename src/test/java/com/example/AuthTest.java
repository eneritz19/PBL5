package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class AuthTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get(URL);

        // Pasar pantalla inicial
        WebElement loginButtonMain = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Login')]")));
        loginButtonMain.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null)
            driver.quit();
    }


    @Test
    @DisplayName("Login ADMIN redirects to admin dashboard")
    void loginAdminRedirectsCorrectly() {
        login("admin@skinXpert.com", "admin123");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("adminDashboard")));
        assertTrue(driver.findElement(By.id("adminDashboard")).isDisplayed());
    }

    @Test
    @DisplayName("Login DOCTOR redirects to doctor dashboard")
    void loginDoctorRedirectsCorrectly() {
        login("jperez@clinic.com", "med123");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));
        assertTrue(driver.findElement(By.id("doctorDashboard")).isDisplayed());
    }

    @Test
    @DisplayName("Login PATIENT redirects to patient dashboard")
    void loginPatientRedirectsCorrectly() {
        login("ana@example.com", "ana123");
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));
        assertTrue(driver.findElement(By.id("patientDashboard")).isDisplayed());
    }


    @Test
    @DisplayName("Logout returns to login screen")
    void logoutWorksCorrectly() {

        // Login como paciente
        login("ana@example.com", "ana123");

        WebElement patientDashboard = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));
        assertTrue(patientDashboard.isDisplayed());

        // Buscar SOLO el botón Logout dentro del dashboard visible
        WebElement logoutButton = wait.until(
                ExpectedConditions.elementToBeClickable(
                        patientDashboard.findElement(
                                By.xpath(".//button[contains(text(),'Log')]"))));

        // Click seguro
        logoutButton.click();

        // Verificar que vuelve al login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPage")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));

        assertTrue(driver.findElement(By.id("email")).isDisplayed());
    }


    @Test
    @DisplayName("Login fails when email is empty")
    void loginFailsWhenEmailEmpty() {
        driver.findElement(By.id("password")).sendKeys("1234");
        driver.findElement(By.id("loginBtn")).click();

        WebElement emailError = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("emailError")));

        assertTrue(emailError.isDisplayed());
    }

    @Test
    @DisplayName("Login fails when password is empty")
    void loginFailsWhenPasswordEmpty() {
        driver.findElement(By.id("email")).sendKeys("test@test.com");
        driver.findElement(By.id("loginBtn")).click();

        WebElement passwordError = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("passwordError")));

        assertTrue(passwordError.isDisplayed());
    }


    private void login(String email, String password) {
        WebElement emailInput = driver.findElement(By.id("email"));
        WebElement passwordInput = driver.findElement(By.id("password"));

        emailInput.clear();
        passwordInput.clear();

        emailInput.sendKeys(email);
        passwordInput.sendKeys(password);
        driver.findElement(By.id("loginBtn")).click();
    }
}
