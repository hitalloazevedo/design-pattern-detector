package com.hitalloazevedo.design_pattern_detector.parser;

import java.util.List;
import java.util.Objects;

public final class SourceInputException
        extends IllegalArgumentException {

    private final List<String> errors;

    public SourceInputException(List<String> errors) {
        super(buildMessage(errors));

        this.errors = List.copyOf(
                Objects.requireNonNull(
                        errors,
                        "errors cannot be null"
                )
        );
    }

    public List<String> errors() {
        return errors;
    }

    private static String buildMessage(List<String> errors) {
        Objects.requireNonNull(
                errors,
                "errors cannot be null"
        );

        return "Invalid source input:%n- %s"
                .formatted(
                        String.join(
                                System.lineSeparator() + "- ",
                                errors
                        )
                );
    }
}