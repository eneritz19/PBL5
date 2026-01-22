package com.example.web;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class PatientDashboardTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));

        driver.get(BASE_URL);

        WebElement loginButtonMain = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Login')]")
        ));
        loginButtonMain.click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email"))).sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");
        driver.findElement(By.id("loginBtn")).click();

        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("patientDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Patient dashboard: History visible")
    void testHistoryIsVisible() {
        
        WebElement historyTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'History')]")
        ));
        historyTab.click();

        WebElement historyList = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("patientHistoryList") 
        ));

        assertTrue(historyList.isDisplayed(), "El historial debería estar visible");
    }

    @Test
    @DisplayName("Patient dashboard: Appointments visible")
    void testAppointmentsAreVisible() {
        
        WebElement appointmentsTab = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Appointments')]")
        ));
        appointmentsTab.click();

        WebElement appointmentsList = wait.until(ExpectedConditions.visibilityOfElementLocated(
                By.id("patientAppointmentsList") 
        ));

        assertTrue(appointmentsList.isDisplayed(), "Las citas deberían estar visibles");
    }
}
