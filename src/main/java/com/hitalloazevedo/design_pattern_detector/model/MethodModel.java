package com.hitalloazevedo.design_pattern_detector.model;

import java.util.List;
import java.util.Objects;

public record MethodModel(
        String name,
        String returnType,
        List<ParameterModel> parameters,
        List<MethodCallModel> methodCalls,
        List<FieldAssignmentModel> fieldAssignments,
        SourceLocation location
) {
    public MethodModel {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(
                returnType,
                "returnType cannot be null"
        );
        Objects.requireNonNull(
                parameters,
                "parameters cannot be null"
        );
        Objects.requireNonNull(
                methodCalls,
                "methodCalls cannot be null"
        );
        Objects.requireNonNull(
                fieldAssignments,
                "fieldAssignments cannot be null"
        );
        Objects.requireNonNull(
                location,
                "location cannot be null"
        );

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }

        if (returnType.isBlank()) {
            throw new IllegalArgumentException(
                    "returnType cannot be blank"
            );
        }

        parameters = List.copyOf(parameters);
        methodCalls = List.copyOf(methodCalls);
        fieldAssignments = List.copyOf(fieldAssignments);
    }

    public boolean callsMethodOnField(FieldModel field) {
        return methodCalls.stream()
                .anyMatch(call ->
                        call.isCalledOn(field.name())
                );
    }

    public boolean assignsParameterToField(
            String parameterName,
            String fieldName
    ) {
        return fieldAssignments.stream()
                .anyMatch(assignment ->
                        assignment.fieldName().equals(fieldName)
                                && assignment.assignsFrom(parameterName)
                );
    }
}