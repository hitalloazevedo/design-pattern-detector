package com.hitalloazevedo.design_pattern_detector.model;

import java.util.List;
import java.util.Objects;

public record ConstructorModel(
        List<ParameterModel> parameters,
        List<FieldAssignmentModel> fieldAssignments,
        SourceLocation location
) {
    public ConstructorModel {
        Objects.requireNonNull(
                parameters,
                "parameters cannot be null"
        );

        Objects.requireNonNull(
                fieldAssignments,
                "fieldAssignments cannot be null"
        );

        Objects.requireNonNull(
                location,
                "location cannot be null"
        );

        parameters = List.copyOf(parameters);
        fieldAssignments = List.copyOf(fieldAssignments);
    }

    public boolean receivesType(String type) {
        return parameters.stream()
                .anyMatch(parameter ->
                        parameter.type().equals(type)
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