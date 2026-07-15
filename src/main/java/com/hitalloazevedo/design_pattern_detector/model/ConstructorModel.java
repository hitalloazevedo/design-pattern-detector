package com.hitalloazevedo.design_pattern_detector.model;

import java.util.List;
import java.util.Objects;

public record ConstructorModel(
        List<ParameterModel> parameters,
        SourceLocation location
) {
    public ConstructorModel {
        Objects.requireNonNull(parameters, "parameters cannot be null");
        Objects.requireNonNull(location, "location cannot be null");

        parameters = List.copyOf(parameters);
    }

    public boolean receivesType(String type) {
        return parameters.stream()
                .anyMatch(parameter -> parameter.type().equals(type));
    }
}