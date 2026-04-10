package com.validation.strategy;

import java.lang.reflect.Field;
import java.util.Optional;
import com.validation.annotation.NrIndeksu;

public class NrIndeksuStrategy implements ValidationStrategy {
    @Override
    public Optional<String> validate(Field field, Object value) {
        if (field.isAnnotationPresent(NrIndeksu.class)) {
            if (value != null && value instanceof String) {
                NrIndeksu annotation = field.getAnnotation(NrIndeksu.class);
                String str = (String) value;
                if (!str.matches("\\d{8}")) {
                    return Optional.of(String.format("Pole %s: %s", field.getName(), annotation.message()));
                }
            }
        }
        return Optional.empty();
    }
}
