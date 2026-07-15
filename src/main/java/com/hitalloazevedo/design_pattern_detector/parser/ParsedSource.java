package com.hitalloazevedo.design_pattern_detector.parser;

import java.nio.file.Path;
import java.util.Objects;

import com.github.javaparser.ast.CompilationUnit;

public record ParsedSource(
        Path sourceFile,
        CompilationUnit compilationUnit
) {
    public ParsedSource {
        Objects.requireNonNull(
                sourceFile,
                "sourceFile cannot be null"
        );
        Objects.requireNonNull(
                compilationUnit,
                "compilationUnit cannot be null"
        );

        sourceFile = sourceFile.toAbsolutePath().normalize();
    }
}