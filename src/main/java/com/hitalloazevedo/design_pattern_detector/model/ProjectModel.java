package com.hitalloazevedo.design_pattern_detector.model;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

public record ProjectModel(
        List<TypeModel> types
) {
    public ProjectModel {
        Objects.requireNonNull(types, "types cannot be null");
        types = List.copyOf(types);
    }

    public Optional<TypeModel> findType(String name) {
        return types.stream()
                .filter(type ->
                        type.name().equals(name)
                                || type.qualifiedName().equals(name)
                )
                .findFirst();
    }

    public List<TypeModel> findImplementationsOf(String typeName) {
        return types.stream()
                .filter(TypeModel::isConcreteClass)
                .filter(type ->
                        type.implementsOrExtends(typeName)
                )
                .toList();
    }

    public List<TypeModel> findTypesWithFieldOfType(
            String typeName
    ) {
        return types.stream()
                .filter(type ->
                        type.hasFieldOfType(typeName)
                )
                .toList();
    }
}