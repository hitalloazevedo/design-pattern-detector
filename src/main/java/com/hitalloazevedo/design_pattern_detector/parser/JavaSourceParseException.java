package com.hitalloazevedo.design_pattern_detector.parser;

import java.nio.file.Path;
import java.util.List;
import java.util.Objects;

import com.github.javaparser.Problem;

public final class JavaSourceParseException
        extends RuntimeException {

    private final Path sourceFile;
    private final List<Problem> problems;

    public JavaSourceParseException(
            Path sourceFile,
            List<Problem> problems
    ) {
        super(buildMessage(sourceFile, problems));

        this.sourceFile = Objects.requireNonNull(
                sourceFile,
                "sourceFile cannot be null"
        );

        this.problems = List.copyOf(
                Objects.requireNonNull(
                        problems,
                        "problems cannot be null"
                )
        );
    }

    public Path sourceFile() {
        return sourceFile;
    }

    public List<Problem> problems() {
        return problems;
    }

    private static String buildMessage(
            Path sourceFile,
            List<Problem> problems
    ) {
        StringBuilder builder = new StringBuilder();

        builder.append("Failed to parse Java source file:")
               .append(System.lineSeparator())
               .append(sourceFile);

        if (!problems.isEmpty()) {
            builder.append(System.lineSeparator())
                   .append(System.lineSeparator())
                   .append("Parser problems:");

            for (Problem problem : problems) {
                builder.append(System.lineSeparator())
                       .append("- ")
                       .append(problem.getVerboseMessage());
            }
        }

        return builder.toString();
    }
}