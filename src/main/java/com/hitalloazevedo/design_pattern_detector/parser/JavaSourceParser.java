package com.hitalloazevedo.design_pattern_detector.parser;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.CompilationUnit;

import java.io.IOException;
import java.nio.file.Path;

public class JavaSourceParser {
    private final JavaParser parser;

    public JavaSourceParser() {
        this.parser = new JavaParser();
    }

    public CompilationUnit parse(Path sourceFile) throws IOException {
        ParseResult<CompilationUnit> result = parser.parse(sourceFile);

        if (result.isSuccessful() && result.getResult().isPresent()) {
            return result.getResult().get();
        }

        String problems = result.getProblems()
                .stream()
                .map(Object::toString)
                .reduce("", (current, problem) ->
                        current + System.lineSeparator() + problem);

        throw new IllegalArgumentException(
                "Não foi possível analisar o arquivo: "
                        + sourceFile
                        + problems
        );
    }
}
