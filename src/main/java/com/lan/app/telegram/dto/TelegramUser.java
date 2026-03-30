package com.lan.app.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUser {
    public Long id;
    public String username;
    public String first_name;
    public String last_name;
    public String language_code;
}
