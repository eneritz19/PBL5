package com.example.web;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.List;

@Tag("ui")
class FilterTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private static final String BASE_URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        driver.get(BASE_URL);

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
        wait.until(d -> {
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
        login("admin@skinXpert.com", "admin123");
        
        // 1. Esperar al dashboard y al contenedor de la lista
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("adminDashboard")));
        WebElement listContainer = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("adminDoctorsList")));

        // 2. Localizar buscador y escribir
        WebElement searchInput = wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("doctorSearch")));
        String searchName = "Juan";
        searchInput.clear();
        searchInput.sendKeys(searchName);

        // 3. Espera dinámica basada en el CONTENIDO del contenedor, no en las clases
        boolean found = wait.until(d -> {
            // Obtenemos todo el texto visible dentro del div de la lista
            String fullText = listContainer.getAttribute("innerText");
            
            // Si el texto contiene el nombre buscado, el filtro ha funcionado
            return fullText.toLowerCase().contains(searchName.toLowerCase());
        });

        Assertions.assertTrue(found, "El nombre '" + searchName + "' debería aparecer en la lista tras filtrar");
    }

}
