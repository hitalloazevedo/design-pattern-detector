package com.hitalloazevedo.design_pattern_detector.detector;

import java.util.List;
import java.util.Optional;

import com.hitalloazevedo.design_pattern_detector.model.ConstructorModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldAssignmentModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodCallModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodModel;
import com.hitalloazevedo.design_pattern_detector.model.ParameterModel;
import com.hitalloazevedo.design_pattern_detector.model.TypeModel;

public final class DetectorSupport {

    private DetectorSupport() {
    }

    public static boolean isAssignedExternally(
            TypeModel type,
            FieldModel field
    ) {
        return findExternalAssignment(type, field).isPresent();
    }

    public static Optional<FieldAssignmentModel> findExternalAssignment(
            TypeModel type,
            FieldModel field
    ) {
        Optional<FieldAssignmentModel> constructorAssignment =
                findConstructorAssignment(type, field);

        if (constructorAssignment.isPresent()) {
            return constructorAssignment;
        }

        return findMethodAssignment(type, field);
    }

    public static List<MethodCallModel> findCallsOnField(
            TypeModel type,
            FieldModel field
    ) {
        return type.methods()
                .stream()
                .flatMap(method -> method.methodCalls().stream())
                .filter(call ->
                        call.referencedFieldName()
                                .map(field.name()::equals)
                                .orElse(false)
                )
                .toList();
    }

    public static List<MethodModel> findMethodsCallingField(
            TypeModel type,
            FieldModel field
    ) {
        return type.methods()
                .stream()
                .filter(method ->
                        method.methodCalls()
                                .stream()
                                .anyMatch(call ->
                                        call.referencedFieldName()
                                                .map(field.name()::equals)
                                                .orElse(false)
                                )
                )
                .toList();
    }

    public static Optional<FieldModel> findInstanceFieldOfType(
            TypeModel type,
            String fieldType
    ) {
        return type.fields()
                .stream()
                .filter(field -> !field.isStatic())
                .filter(field -> field.type().equals(fieldType))
                .findFirst();
    }

    public static List<FieldModel> findInstanceFieldsOfType(
            TypeModel type,
            String fieldType
    ) {
        return type.fields()
                .stream()
                .filter(field -> !field.isStatic())
                .filter(field -> field.type().equals(fieldType))
                .toList();
    }

    private static Optional<FieldAssignmentModel> findConstructorAssignment(
            TypeModel type,
            FieldModel field
    ) {
        return type.constructors()
                .stream()
                .flatMap(constructor ->
                        matchingAssignments(
                                constructor,
                                field
                        ).stream()
                )
                .findFirst();
    }

    private static List<FieldAssignmentModel> matchingAssignments(
            ConstructorModel constructor,
            FieldModel field
    ) {
        return constructor.fieldAssignments()
                .stream()
                .filter(assignment ->
                        assignmentMatchesParameter(
                                assignment,
                                constructor.parameters(),
                                field
                        )
                )
                .toList();
    }

    private static Optional<FieldAssignmentModel> findMethodAssignment(
            TypeModel type,
            FieldModel field
    ) {
        return type.methods()
                .stream()
                .flatMap(method ->
                        method.fieldAssignments()
                                .stream()
                                .filter(assignment ->
                                        assignmentMatchesParameter(
                                                assignment,
                                                method.parameters(),
                                                field
                                        )
                                )
                )
                .findFirst();
    }

    private static boolean assignmentMatchesParameter(
            FieldAssignmentModel assignment,
            List<ParameterModel> parameters,
            FieldModel field
    ) {
        if (!assignment.fieldName().equals(field.name())) {
            return false;
        }

        return assignment.sourceName()
                .map(sourceName ->
                        parameters.stream()
                                .anyMatch(parameter ->
                                        parameter.name().equals(sourceName)
                                                && parameter.type()
                                                        .equals(field.type())
                                )
                )
                .orElse(false);
    }
}