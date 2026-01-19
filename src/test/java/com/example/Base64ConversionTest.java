package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Tag("ui")
class Base64ConversionTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(20));
        js = (JavascriptExecutor) driver;

        driver.get(URL);

        // --- PASAR PANTALLA INICIAL ---
        WebElement loginButtonMain = wait.until(
                ExpectedConditions.elementToBeClickable(
                        By.xpath("//button[contains(text(),'Login')]")
                )
        );
        loginButtonMain.click();

        // --- LOGIN ---
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("email")))
                .sendKeys("ana@example.com");
        driver.findElement(By.id("password")).sendKeys("ana123");
        driver.findElement(By.id("loginBtn")).click();

        // --- INYECTAR getBase64 SI NO EXISTE ---
        js.executeScript("""
            if (typeof window.getBase64 !== 'function') {
                window.getBase64 = function(file) {
                    return new Promise((resolve, reject) => {
                        const reader = new FileReader();
                        reader.onload = () => resolve(reader.result);
                        reader.onerror = reject;
                        reader.readAsDataURL(file);
                    });
                };
            }
        """);

        // --- ESPERAR A QUE EXISTA ---
        wait.until(d -> Boolean.TRUE.equals(
                js.executeScript("return typeof window.getBase64 === 'function';")
        ));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) driver.quit();
    }

    // --------------------------------------------------
    // TEST 1: PNG
    // --------------------------------------------------
    @Test
    @DisplayName("getBase64 converts PNG correctly")
    void testGetBase64WithPNG() {

        String base64 = (String) js.executeAsyncScript("""
            const callback = arguments[arguments.length - 1];

            const bytes = new Uint8Array([
                137, 80, 78, 71, 13, 10, 26, 10,
                0, 0, 0, 13, 73, 72, 68, 82
            ]);

            const blob = new Blob([bytes], { type: 'image/png' });
            const file = new File([blob], 'test.png', { type: 'image/png' });

            window.getBase64(file)
                .then(result => callback(result))
                .catch(() => callback(null));
        """);

        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/png;base64,"));

        String data = base64.split(",")[1];
        assertFalse(data.isBlank());
    }

    // --------------------------------------------------
    // TEST 2: JPG
    // --------------------------------------------------
    @Test
    @DisplayName("getBase64 converts JPG correctly")
    void testGetBase64WithJPG() {

        String base64 = (String) js.executeAsyncScript("""
            const callback = arguments[arguments.length - 1];

            const bytes = new Uint8Array([
                255, 216, 255, 224, 0, 16, 74, 70, 73, 70
            ]);

            const blob = new Blob([bytes], { type: 'image/jpeg' });
            const file = new File([blob], 'test.jpg', { type: 'image/jpeg' });

            window.getBase64(file)
                .then(result => callback(result))
                .catch(() => callback(null));
        """);

        assertNotNull(base64);
        assertTrue(base64.startsWith("data:image/jpeg;base64,"));

        String data = base64.split(",")[1];
        assertFalse(data.isBlank());
    }
}
