package com.hitalloazevedo.design_pattern_detector.model;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

public record TypeModel(
        String name,
        String qualifiedName,
        TypeKind kind,
        List<String> extendedTypes,
        List<String> implementedTypes,
        List<FieldModel> fields,
        List<ConstructorModel> constructors,
        List<MethodModel> methods,
        SourceLocation location
) {
    public TypeModel {
        Objects.requireNonNull(name, "name cannot be null");
        Objects.requireNonNull(
                qualifiedName,
                "qualifiedName cannot be null"
        );
        Objects.requireNonNull(kind, "kind cannot be null");
        Objects.requireNonNull(
                extendedTypes,
                "extendedTypes cannot be null"
        );
        Objects.requireNonNull(
                implementedTypes,
                "implementedTypes cannot be null"
        );
        Objects.requireNonNull(fields, "fields cannot be null");
        Objects.requireNonNull(
                constructors,
                "constructors cannot be null"
        );
        Objects.requireNonNull(methods, "methods cannot be null");
        Objects.requireNonNull(location, "location cannot be null");

        if (name.isBlank()) {
            throw new IllegalArgumentException(
                    "name cannot be blank"
            );
        }

        if (qualifiedName.isBlank()) {
            throw new IllegalArgumentException(
                    "qualifiedName cannot be blank"
            );
        }

        extendedTypes = List.copyOf(extendedTypes);
        implementedTypes = List.copyOf(implementedTypes);
        fields = List.copyOf(fields);
        constructors = List.copyOf(constructors);
        methods = List.copyOf(methods);
    }

    public boolean isConcreteClass() {
        return kind == TypeKind.CLASS
                || kind == TypeKind.RECORD;
    }

    public boolean isAbstraction() {
        return kind == TypeKind.INTERFACE
                || kind == TypeKind.ABSTRACT_CLASS;
    }

    public boolean implementsType(String type) {
        return implementedTypes.contains(type);
    }

    public boolean extendsType(String type) {
        return extendedTypes.contains(type);
    }

    public boolean implementsOrExtends(String type) {
        return implementsType(type) || extendsType(type);
    }

    public Set<String> contracts() {
        Set<String> contracts = new HashSet<>();
        contracts.addAll(implementedTypes);
        contracts.addAll(extendedTypes);
        return Set.copyOf(contracts);
    }

    public Optional<FieldModel> findFieldOfType(String type) {
        return fields.stream()
                .filter(field -> field.type().equals(type))
                .findFirst();
    }

    public boolean hasFieldOfType(String type) {
        return findFieldOfType(type).isPresent();
    }

    public boolean receivesTypeInConstructor(String type) {
        return constructors.stream()
                .anyMatch(constructor ->
                        constructor.receivesType(type)
                );
    }

    public boolean delegatesToField(FieldModel field) {
        return methods.stream()
                .anyMatch(method ->
                        method.callsMethodOnField(field)
                );
    }
}