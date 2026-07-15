package com.hitalloazevedo.design_pattern_detector.parser;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

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

        Set<Path> normalizedRoots =
                normalizeSourceRoots(sourceRoots);

        if (normalizedRoots.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one valid source root is required."
            );
        }

        CombinedTypeSolver typeSolver =
                new CombinedTypeSolver();

        typeSolver.add(new ReflectionTypeSolver());

        for (Path sourceRoot : normalizedRoots) {
            typeSolver.add(
                    new JavaParserTypeSolver(sourceRoot)
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

    public CompilationUnit parse(Path sourceFile) {
        Path normalizedSourceFile =
                validateSourceFile(sourceFile);

        try {
            ParseResult<CompilationUnit> result =
                    parser.parse(normalizedSourceFile);

            if (result.isSuccessful()
                    && result.getResult().isPresent()) {
                return result.getResult().get();
            }

            throw new JavaSourceParseException(
                    normalizedSourceFile,
                    result.getProblems()
            );

        } catch (IOException exception) {
            throw new UncheckedIOException(
                    "Could not read Java source file: "
                            + normalizedSourceFile,
                    exception
            );
        }
    }

    private Set<Path> normalizeSourceRoots(
            Iterable<Path> sourceRoots
    ) {
        Set<Path> normalizedRoots =
                new LinkedHashSet<>();

        for (Path sourceRoot : sourceRoots) {
            if (sourceRoot == null) {
                continue;
            }

            Path normalizedRoot = sourceRoot
                    .toAbsolutePath()
                    .normalize();

            if (Files.isDirectory(normalizedRoot)) {
                normalizedRoots.add(normalizedRoot);
            }
        }

        return normalizedRoots;
    }

    private Path validateSourceFile(Path sourceFile) {
        Objects.requireNonNull(
                sourceFile,
                "sourceFile cannot be null"
        );

        Path normalizedSourceFile = sourceFile
                .toAbsolutePath()
                .normalize();

        if (!Files.isRegularFile(normalizedSourceFile)) {
            throw new IllegalArgumentException(
                    "Java source file does not exist "
                            + "or is not a regular file: "
                            + normalizedSourceFile
            );
        }

        if (!Files.isReadable(normalizedSourceFile)) {
            throw new IllegalArgumentException(
                    "Java source file is not readable: "
                            + normalizedSourceFile
            );
        }

        return normalizedSourceFile;
    }
}