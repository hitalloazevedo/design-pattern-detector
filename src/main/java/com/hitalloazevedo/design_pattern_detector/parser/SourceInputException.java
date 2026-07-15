package com.hitalloazevedo.design_pattern_detector.parser;

import java.util.List;

public final class SourceInputException extends IllegalArgumentException {

    private final List<String> errors;

    public SourceInputException(List<String> errors) {
        super(buildMessage(errors));
        this.errors = List.copyOf(errors);
    }

    public List<String> getErrors() {
        return errors;
    }

    private static String buildMessage(List<String> errors) {
        return "Invalid source input:%n- %s"
                .formatted(String.join(System.lineSeparator() + "- ", errors));
    }
}