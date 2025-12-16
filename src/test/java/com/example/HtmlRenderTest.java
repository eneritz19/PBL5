package com.example;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class HtmlRenderTest {

    @Test
    void indexContainsLoginForm() throws Exception {
        File html = new File(System.getProperty("user.dir"), "src/main/webapp/index.html");
        assertTrue(html.exists(), "El archivo index.html debe existir en src/main/webapp/");

        Document doc = Jsoup.parse(html, "UTF-8");

        // El HTML actual no usa un <form>, los campos son inputs con id y un botón con onclick
        assertNotNull(doc.selectFirst("#email"), "Debe existir un input con id=email");
        assertNotNull(doc.selectFirst("#password"), "Debe existir un input con id=password");

        Element btn = doc.selectFirst("button");
        assertNotNull(btn, "Debe existir un botón en la página");
        String onclick = btn.hasAttr("onclick") ? btn.attr("onclick") : "";
        assertTrue(onclick.contains("handleLogin") || btn.text().toLowerCase().contains("login"), "El botón debe disparar handleLogin() o contener el texto 'Login'");
    }
}
