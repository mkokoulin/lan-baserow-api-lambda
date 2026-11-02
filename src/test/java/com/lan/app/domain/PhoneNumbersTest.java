package com.lan.app.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@DisplayName("PhoneNumbers.normalize")
class PhoneNumbersTest {

    @ParameterizedTest(name = "{0} -> {1}")
    @CsvSource({
        "+374 95 196533,    +37495196533",
        "+37495196533,      +37495196533",
        "37495196533,       +37495196533",
        "095196533,         +37495196533",
        "95196533,          +37495196533",
        "95-196-533,        +37495196533",
    })
    @DisplayName("приводит армянские номера в любом вводимом формате к +374XXXXXXXX")
    void normalizesArmenianNumbers(String input, String expected) {
        assertEquals(expected, PhoneNumbers.normalize(input));
    }

    @ParameterizedTest(name = "{0} -> unchanged")
    @CsvSource({
        "+79161234567",
        "123",
        "tg_12345",
    })
    @DisplayName("нераспознанные/не-армянские номера возвращает как есть")
    void leavesNonArmenianNumbersUnchanged(String input) {
        assertEquals(input, PhoneNumbers.normalize(input));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("null -> null")
    void nullInput_returnsNull() {
        assertNull(PhoneNumbers.normalize(null));
    }

    @org.junit.jupiter.api.Test
    @DisplayName("пустая строка -> пустая строка")
    void blankInput_returnsBlank() {
        assertEquals("", PhoneNumbers.normalize("  "));
    }
}
