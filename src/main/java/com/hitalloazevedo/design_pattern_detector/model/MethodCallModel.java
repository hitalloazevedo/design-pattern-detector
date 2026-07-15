package com.hitalloazevedo.design_pattern_detector.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record MethodCallModel(
        Optional<String> scope,
        Optional<String> scopeType,
        String methodName,
        List<String> argumentTypes,
        SourceLocation location
) {
    public MethodCallModel {
        Objects.requireNonNull(scope, "scope cannot be null");
        Objects.requireNonNull(scopeType, "scopeType cannot be null");
        Objects.requireNonNull(methodName, "methodName cannot be null");
        Objects.requireNonNull(
                argumentTypes,
                "argumentTypes cannot be null"
        );
        Objects.requireNonNull(location, "location cannot be null");

        if (methodName.isBlank()) {
            throw new IllegalArgumentException(
                    "methodName cannot be blank"
            );
        }

        argumentTypes = List.copyOf(argumentTypes);
    }

    public boolean isCalledOn(String expression) {
        return scope
                .map(value ->
                        value.equals(expression)
                                || value.equals("this." + expression)
                )
                .orElse(false);
    }
}