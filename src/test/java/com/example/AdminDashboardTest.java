package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class AdminDashboardTest {

        private WebDriver driver;
        private WebDriverWait wait;
        private static final String BASE_URL = "http://localhost:8080/index.html";

        @BeforeEach
        void setup() {
                driver = new ChromeDriver();
                wait = new WebDriverWait(driver, Duration.ofSeconds(25));
                driver.get(BASE_URL);

                // Pasar pantalla inicial (si existe)
                try {
                        WebElement loginButtonMain = wait.until(ExpectedConditions.elementToBeClickable(
                                        By.xpath("//button[contains(text(),'Login')]")));
                        loginButtonMain.click();
                } catch (TimeoutException ignored) {
                        System.out.println("Pantalla inicial no encontrada, continuando con el flujo.");
                }

                // Esperar login
                wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
        }

        @AfterEach
        void tearDown() {
                if (driver != null)
                        driver.quit();
        }

        @Test
        @DisplayName("Admin can open doctor and patient panels")
        void adminDashboardTest() {

                login("admin@skinXpert.com", "admin123");

                WebElement adminDashboard = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("adminDashboard")));
                assertTrue(adminDashboard.isDisplayed());

                JavascriptExecutor js = (JavascriptExecutor) driver;

                /* ---------- PANEL DOCTORS ---------- */

                WebElement doctorsPanel = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("admin-doctors")));

                WebElement newDoctorBtn = doctorsPanel.findElement(
                                By.xpath(".//button[contains(.,'New Doctor')]"));

                js.executeScript("arguments[0].scrollIntoView({block:'center'});", newDoctorBtn);
                js.executeScript("arguments[0].click();", newDoctorBtn);

                WebElement modal = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("modal")));
                assertTrue(modal.isDisplayed());

                WebElement cancelBtn = modal.findElement(By.xpath(".//button[contains(text(),'Cancel')]"));
                js.executeScript("arguments[0].click();", cancelBtn);
                wait.until(ExpectedConditions.invisibilityOf(modal));

                /* ---------- PANEL PATIENTS ---------- */

                // IMPORTANTE: volver arriba
                js.executeScript("window.scrollTo(0, 0);");

                WebElement patientsTab = adminDashboard.findElement(
                                By.xpath(".//button[contains(.,'Patients')]"));
                js.executeScript("arguments[0].click();", patientsTab);

                WebElement patientsPanel = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("admin-patients")));

                WebElement newPatientBtn = patientsPanel.findElement(
                                By.xpath(".//button[contains(.,'New Patient')]"));

                js.executeScript("arguments[0].scrollIntoView({block:'center'});", newPatientBtn);
                js.executeScript("arguments[0].click();", newPatientBtn);

                WebElement patientModal = wait.until(
                                ExpectedConditions.visibilityOfElementLocated(By.id("modal")));
                assertTrue(patientModal.isDisplayed());

                WebElement patientCancel = patientModal.findElement(
                                By.xpath(".//button[contains(text(),'Cancel')]"));
                js.executeScript("arguments[0].click();", patientCancel);

                wait.until(ExpectedConditions.invisibilityOf(patientModal));
        }

        /* ---------- MÉTODO UTIL ---------- */
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
