package com.hitalloazevedo.design_pattern_detector.detector;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

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
        List<DetectionResult> results =
                new ArrayList<>();

        for (TypeModel type : project.types()) {
            if (!type.isConcreteClass()) {
                continue;
            }

            detectInType(type)
                    .ifPresent(results::add);
        }

        return List.copyOf(results);
    }

    private Optional<DetectionResult> detectInType(
            TypeModel type
    ) {
        for (String contract : findContracts(type)) {
            Optional<FieldModel> wrappedField =
                    DetectorSupport.findInstanceFieldOfType(
                            type,
                            contract
                    );

            if (wrappedField.isEmpty()) {
                continue;
            }

            FieldModel field = wrappedField.get();

            Optional<FieldAssignmentModel> assignment =
                    DetectorSupport.findExternalAssignment(
                            type,
                            field
                    );

            if (assignment.isEmpty()) {
                continue;
            }

            List<MethodCallModel> delegatedCalls =
                    DetectorSupport.findCallsOnField(
                            type,
                            field
                    );

            if (delegatedCalls.isEmpty()) {
                continue;
            }

            return Optional.of(
                    createResult(
                            type,
                            contract,
                            field,
                            assignment.get(),
                            delegatedCalls
                    )
            );
        }

        return Optional.empty();
    }

    private Set<String> findContracts(TypeModel type) {
        Set<String> contracts =
                new LinkedHashSet<>();

        contracts.addAll(type.implementedTypes());
        contracts.addAll(type.extendedTypes());

        return Set.copyOf(contracts);
    }

    private DetectionResult createResult(
            TypeModel type,
            String contract,
            FieldModel field,
            FieldAssignmentModel assignment,
            List<MethodCallModel> delegatedCalls
    ) {
        return new DetectionResult(
                PatternType.DECORATOR,
                type.qualifiedName(),
                buildEvidence(
                        type,
                        contract,
                        field,
                        assignment,
                        delegatedCalls
                ),
                buildAdvantage(
                        type,
                        contract,
                        field
                ),
                buildDisadvantage(
                        type,
                        field,
                        delegatedCalls
                )
        );
    }

    private List<DetectionEvidence> buildEvidence(
            TypeModel type,
            String contract,
            FieldModel field,
            FieldAssignmentModel assignment,
            List<MethodCallModel> delegatedCalls
    ) {
        List<DetectionEvidence> evidence =
                new ArrayList<>();

        evidence.add(
                new DetectionEvidence(
                        "A classe %s implementa ou estende %s."
                                .formatted(
                                        type.qualifiedName(),
                                        contract
                                ),
                        type.location()
                )
        );

        evidence.add(
                new DetectionEvidence(
                        "A classe possui o atributo de instância %s do tipo %s."
                                .formatted(
                                        field.name(),
                                        field.type()
                                ),
                        field.location()
                )
        );

        String sourceName = assignment.sourceName()
                .orElse(assignment.sourceExpression());

        evidence.add(
                new DetectionEvidence(
                        "O parâmetro %s é armazenado no atributo %s."
                                .formatted(
                                        sourceName,
                                        field.name()
                                ),
                        assignment.location()
                )
        );

        for (MethodCallModel call : delegatedCalls) {
            evidence.add(
                    new DetectionEvidence(
                            "Uma operação é delegada para o atributo %s por meio da chamada %s.%s(...)."
                                    .formatted(
                                            field.name(),
                                            call.scope()
                                                    .orElse(
                                                            field.name()
                                                    ),
                                            call.methodName()
                                    ),
                            call.location()
                    )
            );
        }

        return List.copyOf(evidence);
    }

    private String buildAdvantage(
            TypeModel type,
            String contract,
            FieldModel field
    ) {
        return """
                A classe %s pode adicionar comportamento ao contrato %s sem \
                modificar o objeto armazenado no atributo %s. Como ambos seguem \
                o mesmo contrato, os objetos podem ser combinados em cadeia.
                """
                .formatted(
                        type.qualifiedName(),
                        contract,
                        field.name()
                )
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String buildDisadvantage(
            TypeModel type,
            FieldModel field,
            List<MethodCallModel> delegatedCalls
    ) {
        String delegatedCallCount =
                formatDelegatedCallCount(
                        delegatedCalls.size()
                );

        return """
                A classe %s adiciona uma nova camada de delegação por meio do \
                atributo %s. O uso de várias camadas semelhantes pode dificultar \
                o acompanhamento da ordem das chamadas. %s
                """
                .formatted(
                        type.qualifiedName(),
                        field.name(),
                        delegatedCallCount
                )
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String formatDelegatedCallCount(int count) {
        if (count == 1) {
            return "Foi encontrada 1 chamada delegada para esse atributo.";
        }

        return "Foram encontradas %d chamadas delegadas para esse atributo."
                .formatted(count);
    }
}