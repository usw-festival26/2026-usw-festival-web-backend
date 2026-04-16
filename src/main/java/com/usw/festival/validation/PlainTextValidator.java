package com.usw.festival.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.regex.Pattern;

public class PlainTextValidator implements ConstraintValidator<PlainText, String> {

    private static final Pattern HTML_TAG_PATTERN = Pattern.compile("<\\s*/?\\s*[a-zA-Z][^>]*>");

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        return !HTML_TAG_PATTERN.matcher(value).find();
    }
}
