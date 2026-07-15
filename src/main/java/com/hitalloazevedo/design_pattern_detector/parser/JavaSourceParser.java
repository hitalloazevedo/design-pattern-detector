package com.hitalloazevedo.design_pattern_detector.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;
import java.util.stream.Collectors;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.JavaParserTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public final class JavaSourceParser {

    private final JavaParser parser;

    public JavaSourceParser(Iterable<Path> sourceRoots) {
        Objects.requireNonNull(
                sourceRoots,
                "sourceRoots cannot be null"
        );

        CombinedTypeSolver typeSolver =
                new CombinedTypeSolver();

        typeSolver.add(new ReflectionTypeSolver());

        int validRootCount = 0;

        for (Path sourceRoot : sourceRoots) {
            if (sourceRoot == null) {
                continue;
            }

            Path normalizedRoot = sourceRoot
                    .toAbsolutePath()
                    .normalize();

            if (!Files.isDirectory(normalizedRoot)) {
                continue;
            }

            typeSolver.add(
                    new JavaParserTypeSolver(
                            normalizedRoot
                    )
            );

            validRootCount++;
        }

        if (validRootCount == 0) {
            throw new IllegalArgumentException(
                    "At least one valid source root is required."
            );
        }

        JavaSymbolSolver symbolSolver =
                new JavaSymbolSolver(typeSolver);

        ParserConfiguration configuration =
                new ParserConfiguration()
                        .setLanguageLevel(
                                ParserConfiguration.LanguageLevel.JAVA_22
                        )
                        .setSymbolResolver(symbolSolver);

        this.parser = new JavaParser(configuration);
    }

    public CompilationUnit parse(Path sourceFile)
            throws IOException {

        Objects.requireNonNull(
                sourceFile,
                "sourceFile cannot be null"
        );

        Path normalizedSourceFile = sourceFile
                .toAbsolutePath()
                .normalize();

        ParseResult<CompilationUnit> result =
                parser.parse(normalizedSourceFile);

        if (result.isSuccessful()
                && result.getResult().isPresent()) {
            return result.getResult().get();
        }

        String problems = result.getProblems()
                .stream()
                .map(Object::toString)
                .collect(Collectors.joining(
                        System.lineSeparator()
                ));

        throw new IllegalArgumentException(
                "Could not parse Java source file: "
                        + normalizedSourceFile
                        + System.lineSeparator()
                        + problems
        );
    }
}