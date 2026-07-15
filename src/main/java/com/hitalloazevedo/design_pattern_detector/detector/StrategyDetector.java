package com.hitalloazevedo.design_pattern_detector.detector;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.hitalloazevedo.design_pattern_detector.model.FieldAssignmentModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodCallModel;
import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.model.TypeModel;
import com.hitalloazevedo.design_pattern_detector.result.DetectionEvidence;
import com.hitalloazevedo.design_pattern_detector.result.DetectionResult;
import com.hitalloazevedo.design_pattern_detector.result.PatternType;

public final class StrategyDetector
        implements DesignPatternDetector {

    private static final int MINIMUM_IMPLEMENTATIONS = 2;

    @Override
    public List<DetectionResult> detect(ProjectModel project) {
        List<DetectionResult> results = new ArrayList<>();

        for (TypeModel abstraction : findAbstractions(project)) {
            List<TypeModel> implementations =
                    findImplementations(project, abstraction);

            if (implementations.size()
                    < MINIMUM_IMPLEMENTATIONS) {
                continue;
            }

            results.addAll(
                    detectContexts(
                            project,
                            abstraction,
                            implementations
                    )
            );
        }

        return List.copyOf(results);
    }

    private List<TypeModel> findAbstractions(
            ProjectModel project
    ) {
        return project.types()
                .stream()
                .filter(TypeModel::isAbstraction)
                .toList();
    }

    private List<TypeModel> findImplementations(
            ProjectModel project,
            TypeModel abstraction
    ) {
        return project.types()
                .stream()
                .filter(TypeModel::isConcreteClass)
                .filter(type ->
                        type.implementsOrExtends(
                                abstraction.qualifiedName()
                        )
                )
                .toList();
    }

    private List<DetectionResult> detectContexts(
            ProjectModel project,
            TypeModel abstraction,
            List<TypeModel> implementations
    ) {
        List<DetectionResult> results =
                new ArrayList<>();

        for (TypeModel context : project.types()) {
            if (!context.isConcreteClass()) {
                continue;
            }

            /*
             * A Decorator normally implements the same abstraction
             * stored in its field. A Strategy context normally does not.
             */
            if (context.implementsOrExtends(
                    abstraction.qualifiedName()
            )) {
                continue;
            }

            List<FieldModel> strategyFields =
                    DetectorSupport.findInstanceFieldsOfType(
                            context,
                            abstraction.qualifiedName()
                    );

            for (FieldModel strategyField : strategyFields) {
                Optional<DetectionResult> result =
                        detectContext(
                                abstraction,
                                implementations,
                                context,
                                strategyField
                        );

                result.ifPresent(results::add);
            }
        }

        return List.copyOf(results);
    }

    private Optional<DetectionResult> detectContext(
            TypeModel abstraction,
            List<TypeModel> implementations,
            TypeModel context,
            FieldModel strategyField
    ) {
        Optional<FieldAssignmentModel> assignment =
                DetectorSupport.findExternalAssignment(
                        context,
                        strategyField
                );

        if (assignment.isEmpty()) {
            return Optional.empty();
        }

        List<MethodCallModel> delegatedCalls =
                DetectorSupport.findCallsOnField(
                        context,
                        strategyField
                );

        if (delegatedCalls.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(
                createResult(
                        abstraction,
                        implementations,
                        context,
                        strategyField,
                        assignment.get(),
                        delegatedCalls
                )
        );
    }

    private DetectionResult createResult(
            TypeModel abstraction,
            List<TypeModel> implementations,
            TypeModel context,
            FieldModel strategyField,
            FieldAssignmentModel assignment,
            List<MethodCallModel> delegatedCalls
    ) {
        List<DetectionEvidence> evidence =
                buildEvidence(
                        abstraction,
                        implementations,
                        context,
                        strategyField,
                        assignment,
                        delegatedCalls
                );

        return new DetectionResult(
                PatternType.STRATEGY,
                context.qualifiedName(),
                evidence,
                buildAdvantage(
                        abstraction,
                        implementations,
                        context,
                        strategyField
                ),
                buildDisadvantage(
                        abstraction,
                        implementations,
                        context,
                        strategyField
                )
        );
    }

    private List<DetectionEvidence> buildEvidence(
            TypeModel abstraction,
            List<TypeModel> implementations,
            TypeModel context,
            FieldModel strategyField,
            FieldAssignmentModel assignment,
            List<MethodCallModel> delegatedCalls
    ) {
        List<DetectionEvidence> evidence =
                new ArrayList<>();

        evidence.add(
                new DetectionEvidence(
                        "%s é uma abstração com %d implementações concretas."
                                .formatted(
                                        abstraction.qualifiedName(),
                                        implementations.size()
                                ),
                        abstraction.location()
                )
        );

        for (TypeModel implementation : implementations) {
            evidence.add(
                    new DetectionEvidence(
                            "A classe %s implementa ou estende %s."
                                    .formatted(
                                            implementation.qualifiedName(),
                                            abstraction.qualifiedName()
                                    ),
                            implementation.location()
                    )
            );
        }

        evidence.add(
                new DetectionEvidence(
                        "A classe %s possui o atributo de instância %s do tipo %s."
                                .formatted(
                                        context.qualifiedName(),
                                        strategyField.name(),
                                        strategyField.type()
                                ),
                        strategyField.location()
                )
        );

        String sourceName = assignment.sourceName()
                .orElse(assignment.sourceExpression());

        evidence.add(
                new DetectionEvidence(
                        "O parâmetro %s é armazenado no atributo %s."
                                .formatted(
                                        sourceName,
                                        strategyField.name()
                                ),
                        assignment.location()
                )
        );

        for (MethodCallModel call : delegatedCalls) {
            evidence.add(
                    new DetectionEvidence(
                            "A classe %s delega uma operação para o atributo %s por meio da chamada %s.%s(...)."
                                    .formatted(
                                            context.qualifiedName(),
                                            strategyField.name(),
                                            call.scope()
                                                    .orElse(
                                                            strategyField.name()
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
            TypeModel abstraction,
            List<TypeModel> implementations,
            TypeModel context,
            FieldModel strategyField
    ) {
        return """
                A classe %s pode alterar o comportamento utilizado ao substituir \
                o objeto armazenado no atributo %s. Foram encontradas %d \
                implementações concretas de %s, permitindo a troca do \
                comportamento sem modificar a classe de contexto.
                """
                .formatted(
                        context.qualifiedName(),
                        strategyField.name(),
                        implementations.size(),
                        abstraction.qualifiedName()
                )
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String buildDisadvantage(
            TypeModel abstraction,
            List<TypeModel> implementations,
            TypeModel context,
            FieldModel strategyField
    ) {
        String implementationText =
                implementations.size() == 1
                        ? "1 implementação concreta"
                        : "%d implementações concretas"
                                .formatted(
                                        implementations.size()
                                );

        return """
                A estrutura envolve a classe de contexto %s, a abstração %s e %s. \
                Para compreender o comportamento executado, é necessário \
                identificar qual implementação foi atribuída ao atributo %s.
                """
                .formatted(
                        context.qualifiedName(),
                        abstraction.qualifiedName(),
                        implementationText,
                        strategyField.name()
                )
                .replaceAll("\\s+", " ")
                .trim();
    }
}