package com.hitalloazevedo.design_pattern_detector.model;

import java.util.Objects;

public record ParameterModel(
        String name,
        String type
) {
    public ParameterModel {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(type, "type cannot be null");

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