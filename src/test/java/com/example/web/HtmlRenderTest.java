package com.example.web;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.*;

class HtmlRenderTest {

    @Test
    void indexContainsLoginElements() throws Exception {

        File html = new File(System.getProperty("user.dir"),
                "src/main/webapp/index.html");

        assertTrue(html.exists(),
                "index.html debe existir en src/main/webapp/");

        Document doc = Jsoup.parse(html, "UTF-8");

        Element emailInput = doc.selectFirst("#email");
        assertNotNull(emailInput, "Debe existir un input con id='email'");

        Element passwordInput = doc.selectFirst("#password");
        assertNotNull(passwordInput, "Debe existir un input con id='password'");

        Element loginBtn = doc.selectFirst("#loginBtn");

        if (loginBtn == null) {
            loginBtn = doc.selectFirst("button:matchesOwn((?i)login)");
        }

        assertNotNull(loginBtn,
                "Debe existir un botón de login (id='loginBtn' o texto 'Login')");
    }
}
