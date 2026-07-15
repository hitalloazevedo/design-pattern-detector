package com.hitalloazevedo.design_pattern_detector.debug;

import java.util.List;
import java.util.stream.Collectors;

import com.hitalloazevedo.design_pattern_detector.model.ConstructorModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldAssignmentModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodCallModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodModel;
import com.hitalloazevedo.design_pattern_detector.model.ParameterModel;
import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.model.TypeModel;

public final class ProjectModelPrinter {

        public String print(ProjectModel project) {
                StringBuilder output = new StringBuilder();

                output.append("PROJECT MODEL")
                                .append(System.lineSeparator());

                output.append("Types found: ")
                                .append(project.types().size())
                                .append(System.lineSeparator())
                                .append(System.lineSeparator());

                for (TypeModel type : project.types()) {
                        appendType(output, type);
                }

                return output.toString();
        }

        private void appendType(
                        StringBuilder output,
                        TypeModel type) {
                output.append("Type: ")
                                .append(type.qualifiedName())
                                .append(System.lineSeparator());

                output.append("  Kind: ")
                                .append(type.kind())
                                .append(System.lineSeparator());

                output.append("  Extends: ")
                                .append(type.extendedTypes())
                                .append(System.lineSeparator());

                output.append("  Implements: ")
                                .append(type.implementedTypes())
                                .append(System.lineSeparator());

                appendFields(output, type);
                appendConstructors(output, type);
                appendMethods(output, type);

                output.append("  Location: ")
                                .append(type.location().sourceFile())
                                .append(":")
                                .append(type.location().beginLine())
                                .append("-")
                                .append(type.location().endLine())
                                .append(System.lineSeparator())
                                .append(System.lineSeparator());
        }

        private void appendFields(
                        StringBuilder output,
                        TypeModel type) {
                output.append("  Fields:")
                                .append(System.lineSeparator());

                if (type.fields().isEmpty()) {
                        output.append("    none")
                                        .append(System.lineSeparator());
                        return;
                }

                for (FieldModel field : type.fields()) {
                        output.append("    - ")
                                        .append(field.type())
                                        .append(" ")
                                        .append(field.name())
                                        .append(" [line ")
                                        .append(field.location().beginLine())
                                        .append("]")
                                        .append(System.lineSeparator());
                }
        }

        private void appendConstructors(
                        StringBuilder output,
                        TypeModel type) {
                output.append("  Constructors:")
                                .append(System.lineSeparator());

                if (type.constructors().isEmpty()) {
                        output.append("    none")
                                        .append(System.lineSeparator());
                        return;
                }

                for (ConstructorModel constructor : type.constructors()) {

                        output.append("    - parameters: ");

                        if (constructor.parameters().isEmpty()) {
                                output.append("none");
                        } else {
                                output.append(
                                                formatParameters(
                                                                constructor.parameters()));
                        }

                        output.append(" [lines ")
                                        .append(
                                                        constructor.location()
                                                                        .beginLine())
                                        .append("-")
                                        .append(
                                                        constructor.location()
                                                                        .endLine())
                                        .append("]")
                                        .append(System.lineSeparator());

                        appendAssignments(
                                        output,
                                        constructor.fieldAssignments(),
                                        "      ");
                }
        }

        private void appendMethods(
                        StringBuilder output,
                        TypeModel type) {
                output.append("  Methods:")
                                .append(System.lineSeparator());

                if (type.methods().isEmpty()) {
                        output.append("    none")
                                        .append(System.lineSeparator());
                        return;
                }

                for (MethodModel method : type.methods()) {
                        output.append("    - ")
                                        .append(method.returnType())
                                        .append(" ")
                                        .append(method.name())
                                        .append("(")
                                        .append(formatParameters(method.parameters()))
                                        .append(")")
                                        .append(System.lineSeparator());

                        appendMethodCalls(output, method);

                        if (!method.fieldAssignments().isEmpty()) {
                                appendAssignments(
                                                output,
                                                method.fieldAssignments(),
                                                "      ");
                        }
                }
        }

        private void appendMethodCalls(
                        StringBuilder output,
                        MethodModel method) {
                if (method.methodCalls().isEmpty()) {
                        output.append("      calls: none")
                                        .append(System.lineSeparator());
                        return;
                }

                output.append("      calls:")
                                .append(System.lineSeparator());

                for (MethodCallModel call : method.methodCalls()) {
                        output.append("        - ");

                        call.scope().ifPresentOrElse(
                                        scope -> output.append(scope).append("."),
                                        () -> output.append("<implicit>."));

                        output.append(call.methodName())
                                        .append("()");

                        call.scopeType().ifPresent(type -> output.append(" [scope type: ")
                                        .append(type)
                                        .append("]"));

                        call.referencedFieldName().ifPresent(field -> output.append(" [field: ")
                                        .append(field)
                                        .append("]"));

                        output.append(" [line ")
                                        .append(call.location().beginLine())
                                        .append("]")
                                        .append(System.lineSeparator());
                }
        }

        private String formatParameter(
                        ParameterModel parameter) {
                return parameter.type() + " " + parameter.name();
        }

        private String formatParameters(
                        List<ParameterModel> parameters) {
                return parameters.stream()
                                .map(this::formatParameter)
                                .collect(Collectors.joining(", "));
        }

        private void appendAssignments(
                        StringBuilder output,
                        List<FieldAssignmentModel> assignments,
                        String indentation) {
                output.append(indentation)
                                .append("assignments:")
                                .append(System.lineSeparator());

                if (assignments.isEmpty()) {
                        output.append(indentation)
                                        .append("  none")
                                        .append(System.lineSeparator());
                        return;
                }

                for (FieldAssignmentModel assignment : assignments) {

                        output.append(indentation)
                                        .append("  - this.")
                                        .append(assignment.fieldName())
                                        .append(" = ")
                                        .append(
                                                        assignment.sourceExpression());

                        assignment.sourceType().ifPresent(type -> output.append(" [source type: ")
                                        .append(type)
                                        .append("]"));

                        output.append(" [line ")
                                        .append(
                                                        assignment.location()
                                                                        .beginLine())
                                        .append("]")
                                        .append(System.lineSeparator());
                }
        }
}