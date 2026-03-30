package com.lan.app.i18n;

import jakarta.enterprise.context.ApplicationScoped;

import java.util.Locale;
import java.util.ResourceBundle;

@ApplicationScoped
public class I18n {

    public String t(String lang, String key) {
        Locale locale = "ru".equalsIgnoreCase(lang) ? Locale.forLanguageTag("ru") : Locale.ENGLISH;
        ResourceBundle bundle = ResourceBundle.getBundle("i18n.messages", locale);
        return bundle.getString(key);
    }
}
