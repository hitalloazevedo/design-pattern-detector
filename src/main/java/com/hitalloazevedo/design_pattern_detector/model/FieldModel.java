package com.hitalloazevedo.design_pattern_detector.model;

import java.util.Objects;

public record FieldModel(
        String name,
        String type,
        boolean isStatic,
        boolean isFinal,
        SourceLocation location
) {
    public FieldModel {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");
        Objects.requireNonNull(location, "location cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }

        if (type.isBlank()) {
            throw new IllegalArgumentException(
                    "type cannot be blank"
            );
        }
    }
}