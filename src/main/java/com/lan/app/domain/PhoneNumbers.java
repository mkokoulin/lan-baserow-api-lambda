package com.lan.app.domain;

import java.util.regex.Pattern;

/**
 * Canonicalizes Armenian phone numbers to {@code +374XXXXXXXX} so that guests created from any
 * client (site, bot, manual CRM edit) end up with the same stored format and can be found again
 * regardless of how the number was typed. Numbers that aren't Armenian mobile numbers are left
 * untouched (just trimmed) rather than rejected, since guests can have foreign numbers.
 */
public final class PhoneNumbers {

    private static final Pattern AM_PATTERN = Pattern.compile(
            "^\\+374(10|11|33|41|43|44|49|55|77|91|93|94|95|96|98|99)\\d{6}$"
    );

    private PhoneNumbers() {}

    public static String normalize(String input) {
        if (input == null) return null;
        String trimmed = input.trim();
        if (trimmed.isEmpty()) return trimmed;

        boolean hasPlus = trimmed.startsWith("+");
        String digits = trimmed.replaceAll("[^\\d]", "");
        if (digits.isEmpty()) return trimmed;

        String candidate;
        if (hasPlus && digits.startsWith("374")) {
            candidate = "+" + digits;
        } else if (digits.startsWith("374") && digits.length() == 11) {
            candidate = "+" + digits;
        } else if (digits.startsWith("0") && digits.length() == 9) {
            candidate = "+374" + digits.substring(1);
        } else if (digits.length() == 8) {
            candidate = "+374" + digits;
        } else {
            return trimmed;
        }

        return AM_PATTERN.matcher(candidate).matches() ? candidate : trimmed;
    }
}
