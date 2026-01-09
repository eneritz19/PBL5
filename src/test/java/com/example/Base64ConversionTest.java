package com.example;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Tag;

@Tag("ui")
class Base64ConversionTest {

    private WebDriver driver;
    private WebDriverWait wait;
    private JavascriptExecutor js;
    private final String URL = "http://localhost:8080/index.html";

    @BeforeEach
    void setup() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless=new");
        options.addArguments("--no-sandbox");
        options.addArguments("--disable-dev-shm-usage");

        driver = new ChromeDriver(options);
        wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        js = (JavascriptExecutor) driver;

        driver.get(URL);

        // Esperar a que el JS este cargado (despues del splash)
        wait.until(d -> ((JavascriptExecutor) d)
                .executeScript("return typeof getBase64 === 'function';")
                .equals(true));
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }

    // TEST: Conversion Base64

    @Test
    @DisplayName("getBase64 successfully converts an image to Base64")
    void testGetBase64Conversion() {

        String base64 = (String) js.executeAsyncScript(
                """
                        const callback = arguments[arguments.length - 1];

                        // Crear un archivo de imagen fake (PNG)
                        const bytes = new Uint8Array([
                            137, 80, 78, 71, 13, 10, 26, 10,
                            0, 0, 0, 13, 73, 72, 68, 82
                        ]);

                        const blob = new Blob([bytes], { type: 'image/png' });
                        const file = new File([blob], 'test.png', { type: 'image/png' });

                        getBase64(file)
                            .then(result => callback(result))
                            .catch(() => callback(null));
                        """);

        assertNotNull(base64, "The Base64 returned is null");

        assertTrue(
                base64.startsWith("data:image/png;base64,"),
                "Base64 does not have the correct prefix");

        // Verificar que hay datos despues del prefijo
        String data = base64.split(",")[1];
        assertFalse(
                data.isBlank(),
                "Base64 contains no data");
    }
}
