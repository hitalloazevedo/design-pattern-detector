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
            ClassOrInterfaceDeclaration declaration) {
        List<FieldModel> fields = new ArrayList<>();

        declaration.getFields().forEach(fieldDeclaration -> {
            for (VariableDeclarator variable : fieldDeclaration.getVariables()) {

                fields.add(
                        new FieldModel(
                                variable.getNameAsString(),
                                extractTypeName(variable.getType()),
                                locationOf(sourceFile, variable)));
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
                            extractMethodCalls(sourceFile, method),
                            locationOf(sourceFile, method)));
        }

        return List.copyOf(methods);
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
            MethodDeclaration method) {
        return method.findAll(MethodCallExpr.class)
                .stream()
                .map(methodCall -> extractMethodCall(sourceFile, methodCall))
                .toList();
    }

    private MethodCallModel extractMethodCall(
            Path sourceFile,
            MethodCallExpr methodCall) {
        Optional<String> scope = methodCall
                .getScope()
                .map(Expression::toString);

        Optional<String> scopeType = methodCall
                .getScope()
                .flatMap(this::resolveExpressionType);

        List<String> argumentTypes = methodCall
                .getArguments()
                .stream()
                .map(this::extractExpressionType)
                .toList();

        return new MethodCallModel(
                scope,
                scopeType,
                methodCall.getNameAsString(),
                argumentTypes,
                locationOf(sourceFile, methodCall));
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