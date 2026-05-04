package com.energy.marketplace.user.domain.valueObject;

import java.util.regex.Pattern;

public record Email(String value) {

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$"
    );

    public Email {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Email must not be empty");
        }

        value = value.trim().toLowerCase();

        if (!EMAIL_PATTERN.matcher(value).matches()) {
            throw new IllegalArgumentException("Invalid email format");
        }
    }

    public boolean isValid() {
        return EMAIL_PATTERN.matcher(value).matches();
    }
}