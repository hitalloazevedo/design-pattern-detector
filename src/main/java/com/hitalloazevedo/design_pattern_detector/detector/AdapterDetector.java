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

public final class AdapterDetector
        implements DesignPatternDetector {

    @Override
    public List<DetectionResult> detect(ProjectModel project) {
        List<DetectionResult> results = new ArrayList<>();

        for (TypeModel type : project.types()) {
            if (!type.isConcreteClass()) {
                continue;
            }

            results.addAll(
                    detectInType(type)
            );
        }

        return List.copyOf(results);
    }

    private List<DetectionResult> detectInType(
            TypeModel type
    ) {
        List<DetectionResult> results = new ArrayList<>();

        Set<String> contracts = findContracts(type);

        if (contracts.isEmpty()) {
            return List.of();
        }

        for (String contract : contracts) {
            for (FieldModel field : findAdapterFields(type, contract)) {
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

                results.add(
                        createResult(
                                type,
                                contract,
                                field,
                                assignment.get(),
                                delegatedCalls
                        )
                );
            }
        }

        return List.copyOf(results);
    }

    private Set<String> findContracts(TypeModel type) {
        Set<String> contracts = new LinkedHashSet<>();

        contracts.addAll(type.implementedTypes());
        contracts.addAll(type.extendedTypes());

        return Set.copyOf(contracts);
    }

    /**
     * The wrapped/adapted field must be an instance field whose type is
     * different from the contract implemented by the adapter.
     */
    private List<FieldModel> findAdapterFields(
            TypeModel type,
            String contract
    ) {
        return type.fields()
                .stream()
                .filter(field -> !field.isStatic())
                .filter(field ->
                        !field.type().equals(contract)
                )
                .toList();
    }

    private DetectionResult createResult(
            TypeModel type,
            String contract,
            FieldModel adaptedField,
            FieldAssignmentModel assignment,
            List<MethodCallModel> delegatedCalls
    ) {
        return new DetectionResult(
                PatternType.ADAPTER,
                type.qualifiedName(),
                buildEvidence(
                        type,
                        contract,
                        adaptedField,
                        assignment,
                        delegatedCalls
                ),
                buildAdvantage(
                        type,
                        contract,
                        adaptedField
                ),
                buildDisadvantage(
                        type,
                        contract,
                        adaptedField
                )
        );
    }

    private List<DetectionEvidence> buildEvidence(
            TypeModel type,
            String contract,
            FieldModel adaptedField,
            FieldAssignmentModel assignment,
            List<MethodCallModel> delegatedCalls
    ) {
        List<DetectionEvidence> evidence =
                new ArrayList<>();

        evidence.add(
                new DetectionEvidence(
                        "A classe %s implementa ou estende o contrato %s."
                                .formatted(
                                        type.qualifiedName(),
                                        contract
                                ),
                        type.location()
                )
        );

        evidence.add(
                new DetectionEvidence(
                        "A classe possui o atributo de instância %s do tipo %s, diferente do contrato implementado."
                                .formatted(
                                        adaptedField.name(),
                                        adaptedField.type()
                                ),
                        adaptedField.location()
                )
        );

        String sourceName = assignment.sourceName()
                .orElse(assignment.sourceExpression());

        evidence.add(
                new DetectionEvidence(
                        "O parâmetro %s é armazenado no atributo %s."
                                .formatted(
                                        sourceName,
                                        adaptedField.name()
                                ),
                        assignment.location()
                )
        );

        for (MethodCallModel call : delegatedCalls) {
            evidence.add(
                    new DetectionEvidence(
                            "A classe encaminha uma operação para o atributo %s por meio da chamada %s.%s(...)."
                                    .formatted(
                                            adaptedField.name(),
                                            call.scope()
                                                    .orElse(
                                                            adaptedField.name()
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
            FieldModel adaptedField
    ) {
        return """
                A classe %s permite que um objeto do tipo %s seja utilizado \
                por meio do contrato %s. Dessa forma, o código cliente pode \
                trabalhar com a abstração esperada sem depender diretamente \
                do tipo adaptado.
                """
                .formatted(
                        type.qualifiedName(),
                        adaptedField.type(),
                        contract
                )
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String buildDisadvantage(
            TypeModel type,
            String contract,
            FieldModel adaptedField
    ) {
        return """
                A classe %s adiciona uma camada intermediária entre o contrato \
                %s e o tipo %s. Além disso, uma estrutura semelhante também pode \
                ocorrer em classes de serviço que delegam operações para uma \
                dependência, o que pode gerar falsos positivos.
                """
                .formatted(
                        type.qualifiedName(),
                        contract,
                        adaptedField.type()
                )
                .replaceAll("\\s+", " ")
                .trim();
    }
}