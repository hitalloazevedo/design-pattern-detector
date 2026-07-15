package com.hitalloazevedo.design_pattern_detector.parser;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.github.javaparser.Position;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithParameters;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.hitalloazevedo.design_pattern_detector.model.ConstructorModel;
import com.hitalloazevedo.design_pattern_detector.model.FieldModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodCallModel;
import com.hitalloazevedo.design_pattern_detector.model.MethodModel;
import com.hitalloazevedo.design_pattern_detector.model.ParameterModel;
import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.model.SourceLocation;
import com.hitalloazevedo.design_pattern_detector.model.TypeKind;
import com.hitalloazevedo.design_pattern_detector.model.TypeModel;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.hitalloazevedo.design_pattern_detector.model.FieldAssignmentModel;

/**
 * Converts JavaParser AST nodes into the internal structural model used by
 * the pattern detectors.
 *
 * This class only extracts structural information. It does not detect
 * design patterns.
 */
public final class ProjectModelExtractor {

    /**
     * Extracts the structural model from all parsed Java source files.
     *
     * @param sources parsed Java source files
     * @return structural representation of the project
     */
    public ProjectModel extract(List<ParsedSource> sources) {
        Objects.requireNonNull(sources, "sources cannot be null");

        List<TypeModel> types = new ArrayList<>();

        for (ParsedSource source : sources) {
            Objects.requireNonNull(
                    source,
                    "sources cannot contain null elements");

            CompilationUnit compilationUnit = source.compilationUnit();

            List<ClassOrInterfaceDeclaration> declarations = compilationUnit.findAll(
                    ClassOrInterfaceDeclaration.class);

            for (ClassOrInterfaceDeclaration declaration : declarations) {
                TypeModel type = extractType(
                        source.sourceFile(),
                        declaration);

                types.add(type);
            }
        }

        return new ProjectModel(types);
    }

    private TypeModel extractType(
            Path sourceFile,
            ClassOrInterfaceDeclaration declaration) {
        String name = declaration.getNameAsString();

        String qualifiedName = declaration
                .getFullyQualifiedName()
                .orElseGet(() -> buildFallbackQualifiedName(declaration));

        TypeKind kind = determineKind(declaration);

        List<String> extendedTypes = declaration
                .getExtendedTypes()
                .stream()
                .map(this::extractTypeName)
                .toList();

        List<String> implementedTypes = declaration
                .getImplementedTypes()
                .stream()
                .map(this::extractTypeName)
                .toList();

        List<FieldModel> fields = extractFields(
                sourceFile,
                declaration);

        List<ConstructorModel> constructors = extractConstructors(
                sourceFile,
                declaration);

        List<MethodModel> methods = extractMethods(
                sourceFile,
                declaration);

        SourceLocation location = locationOf(
                sourceFile,
                declaration);

        return new TypeModel(
                name,
                qualifiedName,
                kind,
                extendedTypes,
                implementedTypes,
                fields,
                constructors,
                methods,
                location);
    }

    private TypeKind determineKind(
            ClassOrInterfaceDeclaration declaration) {
        if (declaration.isInterface()) {
            return TypeKind.INTERFACE;
        }

        if (declaration.isAbstract()) {
            return TypeKind.ABSTRACT_CLASS;
        }

        return TypeKind.CLASS;
    }

    /**
     * Extracts all fields declared directly by a class or interface.
     *
     * A single declaration may contain multiple variables:
     *
     * private int first, second;
     *
     * In that case, two FieldModel instances are generated.
     */
private List<FieldModel> extractFields(
        Path sourceFile,
        ClassOrInterfaceDeclaration declaration
) {
    List<FieldModel> fields = new ArrayList<>();

    declaration.getFields().forEach(fieldDeclaration -> {
        boolean isStatic = fieldDeclaration.isStatic();
        boolean isFinal = fieldDeclaration.isFinal();

        for (VariableDeclarator variable
                : fieldDeclaration.getVariables()) {

            fields.add(
                    new FieldModel(
                            variable.getNameAsString(),
                            extractTypeName(variable.getType()),
                            isStatic,
                            isFinal,
                            locationOf(sourceFile, variable)
                    )
            );
        }
    });

    return List.copyOf(fields);
}

    private List<ConstructorModel> extractConstructors(
            Path sourceFile,
            ClassOrInterfaceDeclaration declaration) {
        List<ConstructorModel> constructors = new ArrayList<>();

        for (ConstructorDeclaration constructor : declaration.getConstructors()) {

            constructors.add(
                    new ConstructorModel(
                            extractParameters(constructor),
                            extractFieldAssignments(
                                    sourceFile,
                                    constructor),
                            locationOf(sourceFile, constructor)));
        }

        return List.copyOf(constructors);
    }

    private List<MethodModel> extractMethods(
            Path sourceFile,
            ClassOrInterfaceDeclaration declaration) {
        List<MethodModel> methods = new ArrayList<>();

        for (MethodDeclaration method : declaration.getMethods()) {
            methods.add(
                    new MethodModel(
                            method.getNameAsString(),
                            extractTypeName(method.getType()),
                            extractParameters(method),
                            extractMethodCalls(
                                    sourceFile,
                                    declaration,
                                    method),
                            extractFieldAssignments(
                                    sourceFile,
                                    method),
                            locationOf(sourceFile, method)));
        }

        return List.copyOf(methods);
    }

    private List<FieldAssignmentModel> extractFieldAssignments(
            Path sourceFile,
            Node callable) {
        return callable.findAll(AssignExpr.class)
                .stream()
                .map(assignment -> extractFieldAssignment(
                        sourceFile,
                        assignment))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<FieldAssignmentModel> extractFieldAssignment(
            Path sourceFile,
            AssignExpr assignment) {
        Optional<String> fieldName = extractAssignedFieldName(
                assignment.getTarget());

        if (fieldName.isEmpty()) {
            return Optional.empty();
        }

        Expression value = assignment.getValue();

        Optional<String> sourceName = extractSourceName(value);

        Optional<String> sourceType = resolveExpressionType(value);

        return Optional.of(
                new FieldAssignmentModel(
                        fieldName.get(),
                        value.toString(),
                        sourceName,
                        sourceType,
                        locationOf(sourceFile, assignment)));
    }

    private Optional<String> extractAssignedFieldName(
            Expression target) {
        if (target.isFieldAccessExpr()) {
            FieldAccessExpr fieldAccess = target.asFieldAccessExpr();

            if (fieldAccess.getScope().isThisExpr()) {
                return Optional.of(
                        fieldAccess.getNameAsString());
            }

            return Optional.empty();
        }

        if (target.isNameExpr()) {
            NameExpr nameExpression = target.asNameExpr();

            try {
                var resolved = nameExpression.resolve();

                if (resolved.isField()) {
                    return Optional.of(
                            resolved.getName());
                }
            } catch (RuntimeException exception) {
                /*
                 * Without symbol resolution, a simple target could be
                 * either a local variable or a field. Do not guess here.
                 */
                return Optional.empty();
            }
        }

        return Optional.empty();
    }

    private Optional<String> extractSourceName(
            Expression expression) {
        if (expression.isNameExpr()) {
            return Optional.of(
                    expression.asNameExpr()
                            .getNameAsString());
        }

        return Optional.empty();
    }

    private List<ParameterModel> extractParameters(
            NodeWithParameters<?> node) {
        return node.getParameters()
                .stream()
                .map(parameter -> new ParameterModel(
                        parameter.getNameAsString(),
                        extractTypeName(parameter.getType())))
                .toList();
    }

    private List<MethodCallModel> extractMethodCalls(
            Path sourceFile,
            ClassOrInterfaceDeclaration owner,
            MethodDeclaration method) {
        return method.findAll(MethodCallExpr.class)
                .stream()
                .map(methodCall -> extractMethodCall(
                        sourceFile,
                        owner,
                        methodCall))
                .toList();
    }

    private MethodCallModel extractMethodCall(
            Path sourceFile,
            ClassOrInterfaceDeclaration owner,
            MethodCallExpr methodCall) {
        Optional<Expression> scopeExpression = methodCall.getScope();

        Optional<String> scope = scopeExpression
                .map(Expression::toString);

        Optional<String> scopeType = scopeExpression
                .flatMap(this::resolveExpressionType);

        Optional<String> referencedFieldName = scopeExpression.flatMap(scopeValue -> resolveReferencedFieldName(
                scopeValue,
                owner));

        List<String> argumentTypes = methodCall
                .getArguments()
                .stream()
                .map(this::extractExpressionType)
                .toList();

        return new MethodCallModel(
                scope,
                scopeType,
                referencedFieldName,
                methodCall.getNameAsString(),
                argumentTypes,
                locationOf(sourceFile, methodCall));
    }

    private Optional<String> resolveReferencedFieldName(
            Expression scope,
            ClassOrInterfaceDeclaration owner) {
        Optional<String> candidate = extractSimpleScopeName(scope);

        if (candidate.isEmpty()) {
            return Optional.empty();
        }

        String fieldName = candidate.get();

        boolean declaredField = owner.getFields()
                .stream()
                .flatMap(field -> field.getVariables().stream())
                .anyMatch(variable -> variable.getNameAsString()
                        .equals(fieldName));

        return declaredField
                ? Optional.of(fieldName)
                : Optional.empty();
    }

    private Optional<String> extractSimpleScopeName(
            Expression scope) {
        if (scope.isNameExpr()) {
            return Optional.of(
                    scope.asNameExpr()
                            .getNameAsString());
        }

        if (scope.isFieldAccessExpr()) {
            FieldAccessExpr fieldAccess = scope.asFieldAccessExpr();

            if (fieldAccess.getScope().isThisExpr()) {
                return Optional.of(
                        fieldAccess.getNameAsString());
            }
        }

        return Optional.empty();
    }

    /**
     * Attempts to resolve the type of an expression using the Symbol Solver.
     *
     * Resolution can fail when the parser has not been configured with a
     * symbol solver or when external dependencies are unavailable. In that
     * case, Optional.empty() is returned.
     */
    private Optional<String> resolveExpressionType(
            Expression expression) {
        try {
            return Optional.of(
                    expression.calculateResolvedType().describe());
        } catch (RuntimeException exception) {
            return Optional.empty();
        }
    }

    /**
     * Attempts to determine an argument's type. When resolution is not
     * possible, a basic syntactic description is used as a fallback.
     */
    private String extractExpressionType(Expression expression) {
        try {
            return expression.calculateResolvedType().describe();
        } catch (RuntimeException exception) {
            return inferExpressionType(expression);
        }
    }

    private String inferExpressionType(Expression expression) {
        if (expression.isStringLiteralExpr()) {
            return "java.lang.String";
        }

        if (expression.isIntegerLiteralExpr()) {
            return "int";
        }

        if (expression.isLongLiteralExpr()) {
            return "long";
        }

        if (expression.isDoubleLiteralExpr()) {
            return "double";
        }

        if (expression.isBooleanLiteralExpr()) {
            return "boolean";
        }

        if (expression.isCharLiteralExpr()) {
            return "char";
        }

        if (expression.isNullLiteralExpr()) {
            return "null";
        }

        if (expression.isObjectCreationExpr()) {
            return expression
                    .asObjectCreationExpr()
                    .getType()
                    .asString();
        }

        if (expression.isCastExpr()) {
            return expression
                    .asCastExpr()
                    .getType()
                    .asString();
        }

        if (expression.isArrayCreationExpr()) {
            return expression
                    .asArrayCreationExpr()
                    .getElementType()
                    .asString()
                    + "[]";
        }

        /*
         * Without symbol resolution, the actual type of a variable or
         * complex expression may be unknown. The expression category is
         * preserved so the information is not completely lost.
         */
        return "<unresolved:"
                + expression.getClass().getSimpleName()
                + ">";
    }

    /**
     * Resolves a declared type when possible and falls back to its source
     * representation when symbol resolution is unavailable.
     */
    private String extractTypeName(Type type) {
        try {
            return type.resolve().describe();
        } catch (RuntimeException exception) {
            return type.asString();
        }
    }

    private String extractTypeName(
            ClassOrInterfaceType type) {
        try {
            return type.resolve().describe();
        } catch (RuntimeException exception) {
            return type.asString();
        }
    }

    private SourceLocation locationOf(
            Path sourceFile,
            Node node) {
        Position begin = node.getBegin()
                .orElseThrow(() -> new IllegalStateException(
                        "AST node does not have a beginning "
                                + "position: "
                                + node.getClass().getSimpleName()));

        Position end = node.getEnd()
                .orElseThrow(() -> new IllegalStateException(
                        "AST node does not have an ending "
                                + "position: "
                                + node.getClass().getSimpleName()));

        return new SourceLocation(
                sourceFile,
                begin.line,
                end.line);
    }

    /**
     * Used when JavaParser cannot provide a fully qualified name, which can
     * happen with nested or local types.
     */
    private String buildFallbackQualifiedName(
            ClassOrInterfaceDeclaration declaration) {
        String packageName = declaration
                .findCompilationUnit()
                .flatMap(CompilationUnit::getPackageDeclaration)
                .map(packageDeclaration -> packageDeclaration.getNameAsString())
                .orElse("");

        List<String> enclosingTypes = new ArrayList<>();

        Optional<Node> currentNode = declaration.getParentNode();

        while (currentNode.isPresent()) {
            Node parent = currentNode.get();

            if (parent instanceof ClassOrInterfaceDeclaration parentType) {
                enclosingTypes.add(
                        0,
                        parentType.getNameAsString());
            }

            currentNode = parent.getParentNode();
        }

        StringBuilder name = new StringBuilder();

        if (!packageName.isBlank()) {
            name.append(packageName).append('.');
        }

        if (!enclosingTypes.isEmpty()) {
            name.append(String.join(".", enclosingTypes))
                    .append('.');
        }

        name.append(declaration.getNameAsString());

        return name.toString();
    }
}