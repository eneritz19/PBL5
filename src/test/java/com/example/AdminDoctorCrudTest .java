package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class AdminDoctorCrudTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    // Datos dinámicos (MUY importante)
    private String doctorName;
    private String doctorEmail;

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get(URL);

        // --- Login como ADMIN ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("admin@skinXpert.com");
        driver.findElement(By.id("password")).sendKeys("admin123");
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // Esperar dashboard admin
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("adminDashboard")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    @Test
    @DisplayName("Admin can create and delete a doctor safely")
    void testCreateAndDeleteDoctor() {

        // --- Generar doctor de test ---
        String uid = UUID.randomUUID().toString().substring(0, 6);
        doctorName = "Test Doctor " + uid;
        doctorEmail = "testdoctor_" + uid + "@example.com";

        // --- Click en New Doctor ---
        WebElement newBtn = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'New')]")
                )
        );
        newBtn.click();

        // --- Rellenar formulario ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("new_doctor_code")))
                .sendKeys("TD-" + uid);

        driver.findElement(By.id("new_doctor_name")).sendKeys(doctorName);
        driver.findElement(By.id("new_doctor_email")).sendKeys(doctorEmail);
        driver.findElement(By.id("new_doctor_password")).sendKeys("test123");

        // --- Guardar ---
        driver.findElement(By.id("modalSaveBtn")).click();

        // --- Aceptar alert de creación ---
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        // --- Verificar que aparece en la lista ---
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                By.id("adminDoctorsList"),
                doctorEmail
        ));

        assertTrue(
                driver.findElement(By.id("adminDoctorsList"))
                        .getText().contains(doctorEmail),
                "El doctor creado no aparece en la lista"
        );

        // --- Localizar botón Delete SOLO de este doctor ---
        WebElement doctorCard = driver.findElement(
                By.xpath("//div[@id='adminDoctorsList']//strong[text()='" + doctorName + "']/ancestor::div[contains(@class,'case-card')]")
        );

        WebElement deleteBtn = doctorCard.findElement(
                By.xpath(".//button[contains(text(),'Delete')]")
        );

        deleteBtn.click();

        // --- Confirmar confirm() ---
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        // --- Aceptar alert de éxito ---
        wait.until(ExpectedConditions.alertIsPresent());
        driver.switchTo().alert().accept();

        // --- Verificar que ya NO existe ---
        wait.until(ExpectedConditions.invisibilityOfElementLocated(
                By.xpath("//strong[text()='" + doctorName + "']")
        ));
    }
}
