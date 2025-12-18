package com.example;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ServerErrorTest {

    private WebDriver driver;
    private WebDriverWait wait;
    // Asegúrate de que tu HTML se esté sirviendo, pero Node-RED (puerto 1880) esté APAGADO
    private final String URL = "http://localhost:8080/index.html"; 

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.get(URL);
        
        // Esperamos a que pase el Splash Screen para interactuar con el login
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    @Test
    void shouldShowServerErrorWhenNodeRedIsDown() {
        // 1. Introducir cualquier dato
        driver.findElement(By.id("email")).sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");

        // 2. Intentar hacer login
        driver.findElement(By.xpath("//button[text()='Login']")).click();

        // 3. Verificar que aparece el alert de error de conexión
        // Selenium esperará a que el navegador intente conectar y falle (el catch en tu JS)
        wait.until(ExpectedConditions.alertIsPresent());
        
        String alertText = driver.switchTo().alert().getText();
        
        // Verificamos que el mensaje coincide exactamente con tu código JS
        assertEquals("Server connection error.", alertText);
        
        // Aceptamos la alerta para cerrar el test
        driver.switchTo().alert().accept();
    }
}
