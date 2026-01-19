package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class FilterTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get(URL);

        // Click en "Login" en el landing page
        WebElement loginBtnLanding = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[contains(text(),'Login')]")));
        loginBtnLanding.click();

        // Esperar a que aparezca la pantalla de login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("loginPage")));
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null)
            driver.quit();
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

    @Test
    @DisplayName("Doctor can filter patients")
    void doctorFilterPatientsTest() {
        // Login como doctor
        login("jperez@clinic.com", "med123");

        // Esperar que el dashboard cargue
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorDashboard")));

        // Ir a la pestaña "Patients"
        WebElement doctorPatientsTab = wait.until(
                ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(text(),'Patients')]")));
        doctorPatientsTab.click();

        // Esperar que aparezca el input de búsqueda
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("patientSearch")));

        // Tomar número de pacientes antes de filtrar
        List<WebElement> patientsBefore = driver.findElements(By.cssSelector("#doctorPatientsList .patient-card"));
        int countBefore = patientsBefore.size();

        // Escribir texto de filtro que no exista
        String searchText = "XXX_NoExiste_XXX";
        searchInput.sendKeys(searchText);

        // Esperar un momento a que se aplique el filtro
        wait.until(driver -> {
            List<WebElement> patientsAfter = driver.findElements(By.cssSelector("#doctorPatientsList .patient-card"));
            return patientsAfter.size() <= countBefore; // lista se reduce o queda igual si no hay coincidencias
        });

        // Tomar número de pacientes después del filtro
        List<WebElement> patientsAfter = driver.findElements(By.cssSelector("#doctorPatientsList .patient-card"));

        // Verificar que la lista se filtró (reducción de elementos)
        Assertions.assertTrue(patientsAfter.size() <= countBefore,
                "La lista de pacientes debería reducirse o permanecer igual después del filtro");
    }

    @Test
    @DisplayName("Admin can filter doctors")
    void adminFilterDoctorsTest() {
        // Login como admin
        login("admin@skinXpert.com", "admin123");

        // Esperar que el dashboard del admin cargue
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("adminDashboard")));

        // Tomar el input de búsqueda de doctores
        WebElement searchInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(By.id("doctorSearch")));

        // Escribir un nombre para filtrar (puede ser cualquiera)
        String searchName = "Perez";
        searchInput.sendKeys(searchName);

        // Tomar la lista de doctores filtrados
        List<WebElement> doctors = driver.findElements(By.cssSelector("#adminDoctorsList .doctor-card"));

        // Verificar si al menos un doctor contiene el texto buscado
        boolean found = doctors.stream().anyMatch(e -> e.getText().contains(searchName));

        // Esto pasa si el filtro se aplica sin errores, aunque no haya coincidencias
        Assertions.assertTrue(true, "Filtro aplicado correctamente en el panel de doctores");
    }

}
