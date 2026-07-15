package com.hitalloazevedo.design_pattern_detector.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public record SourceInput(
        List<Path> javaFiles,
        Set<Path> providedDirectories
) {
    public SourceInput {
        Objects.requireNonNull(
                javaFiles,
                "javaFiles cannot be null"
        );

        Objects.requireNonNull(
                providedDirectories,
                "providedDirectories cannot be null"
        );

        javaFiles = List.copyOf(javaFiles);
        providedDirectories = Set.copyOf(providedDirectories);
    }
}