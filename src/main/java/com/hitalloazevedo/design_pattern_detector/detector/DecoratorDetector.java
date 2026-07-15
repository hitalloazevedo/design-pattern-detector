package com.hitalloazevedo.design_pattern_detector.detector;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.hitalloazevedo.design_pattern_detector.model.ConstructorModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldAssignmentModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodCallModel;
import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.model.TypeModel;
import com.hitalloazevedo.design_pattern_detector.result.DetectionEvidence;
import com.hitalloazevedo.design_pattern_detector.result.DetectionResult;
import com.hitalloazevedo.design_pattern_detector.result.PatternType;

public final class DecoratorDetector
        implements DesignPatternDetector {

    @Override
    public List<DetectionResult> detect(ProjectModel project) {
        List<DetectionResult> results = new ArrayList<>();

        for (TypeModel type : project.types()) {
            if (!type.isConcreteClass()) {
                continue;
            }

            detectInType(type)
                    .ifPresent(results::add);
        }

        return List.copyOf(results);
    }

    private java.util.Optional<DetectionResult> detectInType(
            TypeModel type) {
        for (String contract : findContracts(type)) {
            java.util.Optional<FieldModel> wrappedField = findWrappedField(type, contract);

            if (wrappedField.isEmpty()) {
                continue;
            }

            FieldModel field = wrappedField.get();

            if (!isAssignedExternally(type, field)) {
                continue;
            }

            List<MethodCallModel> delegatedCalls = findDelegatedCalls(type, field);

            if (delegatedCalls.isEmpty()) {
                continue;
            }

            return java.util.Optional.of(
                    createResult(
                            type,
                            contract,
                            field,
                            delegatedCalls));
        }

        return java.util.Optional.empty();
    }

    /**
     * Returns the abstractions implemented or extended by the candidate.
     */
    private Set<String> findContracts(TypeModel type) {
        Set<String> contracts = new LinkedHashSet<>();

        contracts.addAll(type.implementedTypes());
        contracts.addAll(type.extendedTypes());

        return Set.copyOf(contracts);
    }

    /**
     * A Decorator must contain an instance field whose type is the same
     * abstraction implemented or extended by the class.
     */
    private java.util.Optional<FieldModel> findWrappedField(
            TypeModel type,
            String contract) {
        return type.fields()
                .stream()
                .filter(field -> !field.isStatic())
                .filter(field -> field.type().equals(contract))
                .findFirst();
    }

    /**
     * Verifies whether the wrapped object is assigned from a constructor
     * parameter or method parameter.
     */
    private boolean isAssignedExternally(
            TypeModel type,
            FieldModel field) {
        return isAssignedInConstructor(type, field)
                || isAssignedInMethod(type, field);
    }

    private boolean isAssignedInConstructor(
            TypeModel type,
            FieldModel field) {
        return type.constructors()
                .stream()
                .anyMatch(constructor -> constructorAssignsParameterToField(
                        constructor,
                        field));
    }

    private boolean constructorAssignsParameterToField(
            ConstructorModel constructor,
            FieldModel field) {
        return constructor.fieldAssignments()
                .stream()
                .anyMatch(assignment -> assignmentComesFromCompatibleParameter(
                        assignment,
                        constructor.parameters(),
                        field));
    }

    private boolean isAssignedInMethod(
            TypeModel type,
            FieldModel field) {
        return type.methods()
                .stream()
                .anyMatch(method -> method.fieldAssignments()
                        .stream()
                        .anyMatch(assignment -> assignmentComesFromCompatibleParameter(
                                assignment,
                                method.parameters(),
                                field)));
    }

    private boolean assignmentComesFromCompatibleParameter(
            FieldAssignmentModel assignment,
            List<com.hitalloazevedo.design_pattern_detector.model.ParameterModel> parameters,
            FieldModel field) {
        if (!assignment.fieldName().equals(field.name())) {
            return false;
        }

        return assignment.sourceName()
                .map(sourceName -> parameters.stream()
                        .anyMatch(parameter -> parameter.name().equals(sourceName)
                                && parameter.type()
                                        .equals(field.type())))
                .orElse(false);
    }

    /**
     * Finds method calls whose target is the wrapped field.
     */
    private List<MethodCallModel> findDelegatedCalls(
            TypeModel type,
            FieldModel field) {
        return type.methods()
                .stream()
                .flatMap(method -> method.methodCalls().stream())
                .filter(call -> call.referencedFieldName()
                        .map(field.name()::equals)
                        .orElse(false))
                .toList();
    }

    private DetectionResult createResult(
            TypeModel type,
            String contract,
            FieldModel field,
            List<MethodCallModel> delegatedCalls) {
        List<DetectionEvidence> evidence = buildEvidence(
                type,
                contract,
                field,
                delegatedCalls);

        String advantage = buildAdvantage(
                type,
                contract,
                field);

        String disadvantage = buildDisadvantage(
                type,
                field,
                delegatedCalls);

        return new DetectionResult(
                PatternType.DECORATOR,
                type.qualifiedName(),
                evidence,
                advantage,
                disadvantage);
    }

    private List<DetectionEvidence> buildEvidence(
            TypeModel type,
            String contract,
            FieldModel field,
            List<MethodCallModel> delegatedCalls) {
        List<DetectionEvidence> evidence = new ArrayList<>();

        evidence.add(
                new DetectionEvidence(
                        "A classe %s implementa ou estende %s."
                                .formatted(
                                        type.qualifiedName(),
                                        contract),
                        type.location()));

        evidence.add(
                new DetectionEvidence(
                        "A classe possui o atributo de instância %s do tipo %s."
                                .formatted(
                                        field.name(),
                                        field.type()),
                        field.location()));
        findFieldAssignment(type, field)
                .ifPresent(assignment -> {

                    String parameterName = assignment.sourceName()
                            .orElse(assignment.sourceExpression());

                    evidence.add(
                            new DetectionEvidence(
                                    "O parâmetro %s é armazenado no atributo %s."
                                            .formatted(
                                                    parameterName,
                                                    field.name()),
                                    assignment.location()));
                });

        for (MethodCallModel call : delegatedCalls) {
            evidence.add(
                    new DetectionEvidence(
                            "Uma operação é delegada para o atributo %s por meio da chamada %s.%s(...)."
                                    .formatted(
                                            field.name(),
                                            call.scope()
                                                    .orElse(field.name()),
                                            call.methodName()),
                            call.location()));
        }

        return List.copyOf(evidence);
    }

    private java.util.Optional<FieldAssignmentModel> findFieldAssignment(
            TypeModel type,
            FieldModel field) {
        java.util.Optional<FieldAssignmentModel> constructorAssignment = type.constructors()
                .stream()
                .flatMap(constructor -> constructor.fieldAssignments()
                        .stream())
                .filter(assignment -> assignment.fieldName()
                        .equals(field.name()))
                .findFirst();

        if (constructorAssignment.isPresent()) {
            return constructorAssignment;
        }

        return type.methods()
                .stream()
                .flatMap(method -> method.fieldAssignments()
                        .stream())
                .filter(assignment -> assignment.fieldName()
                        .equals(field.name()))
                .findFirst();
    }

    private String buildAdvantage(
            TypeModel type,
            String contract,
            FieldModel field) {
        return """
                A classe %s pode adicionar comportamento ao contrato %s \
                sem modificar o objeto armazenado no atributo %s. Como ambos \
                seguem o mesmo contrato, os objetos podem ser combinados em cadeia.
                """.formatted(
                type.qualifiedName(),
                contract,
                field.name()).replaceAll("\\s+", " ").trim();
    }

    private String buildDisadvantage(
            TypeModel type,
            FieldModel field,
            List<MethodCallModel> delegatedCalls) {
        String callCount = formatDelegatedCallCount(delegatedCalls.size());

        return """
                A classe %s adiciona uma nova camada de delegação por meio do \
                atributo %s. O uso de várias camadas semelhantes pode dificultar \
                o acompanhamento da ordem das chamadas. %s
                """.formatted(
                type.qualifiedName(),
                field.name(),
                callCount).replaceAll("\\s+", " ").trim();
    }

    private String formatDelegatedCallCount(int count) {
        return count == 1
                ? "Foi encontrada 1 chamada delegada para esse atributo."
                : "Foram encontradas %d chamadas delegadas para esse atributo."
                        .formatted(count);
    }
}