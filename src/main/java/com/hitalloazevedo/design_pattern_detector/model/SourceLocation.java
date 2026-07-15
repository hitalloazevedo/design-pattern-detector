package com.hitalloazevedo.design_pattern_detector.model;

import java.nio.file.Path;
import java.util.Objects;

public record SourceLocation(
        Path sourceFile,
        int beginLine,
        int endLine
) {
    public SourceLocation {
        Objects.requireNonNull(sourceFile, "sourceFile cannot be null");

        if (beginLine < 1) {
            throw new IllegalArgumentException(
                    "beginLine must be greater than zero"
            );
        }

        if (endLine < beginLine) {
            throw new IllegalArgumentException(
                    "endLine cannot be smaller than beginLine"
            );
        }

        sourceFile = sourceFile.toAbsolutePath().normalize();
    }
}