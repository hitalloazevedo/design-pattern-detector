package com.hitalloazevedo.design_pattern_detector.model;

import java.util.Objects;
import java.util.Optional;

public record FieldAssignmentModel(
        String fieldName,
        String sourceExpression,
        Optional<String> sourceName,
        Optional<String> sourceType,
        SourceLocation location
) {
    public FieldAssignmentModel {
        Objects.requireNonNull(
                fieldName,
                "fieldName cannot be null"
        );

        Objects.requireNonNull(
                sourceExpression,
                "sourceExpression cannot be null"
        );

        Objects.requireNonNull(
                sourceName,
                "sourceName cannot be null"
        );

        Objects.requireNonNull(
                sourceType,
                "sourceType cannot be null"
        );

        Objects.requireNonNull(
                location,
                "location cannot be null"
        );

        if (fieldName.isBlank()) {
            throw new IllegalArgumentException(
                    "fieldName cannot be blank"
            );
        }

        if (sourceExpression.isBlank()) {
            throw new IllegalArgumentException(
                    "sourceExpression cannot be blank"
            );
        }
    }

    public boolean assignsFrom(String variableName) {
        return sourceName
                .map(variableName::equals)
                .orElse(false);
    }
}