package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;
import com.validation.annotation.Email;

public class EmailStrategy implements ValidationStrategy {
    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(Email.class)) {
            if (value != null && value instanceof String) {
                Email annotation = field.getAnnotation(Email.class);
                String str = (String) value;
                // Proste wyrażenie regularne dla adresu email
                if (!str.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$")) {
                    return Optional.of(String.format("Pole %s: %s", field.getName(), annotation.message()));
                }
            }
        }
        return Optional.empty();
    }
}
