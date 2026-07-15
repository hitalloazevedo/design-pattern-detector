package com.hitalloazevedo.design_pattern_detector.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public final class SourceInputHandler {

    public List<Path> resolve(String[] arguments) {
        if (arguments == null || arguments.length == 0) {
            throw new IllegalArgumentException(
                    "At least one Java file or directory must be provided."
            );
        }

        Set<Path> javaFiles = new LinkedHashSet<>();
        List<String> errors = new ArrayList<>();

        for (String argument : arguments) {
            if (argument == null || argument.isBlank()) {
                errors.add("An empty path was provided.");
                continue;
            }

            try {
                Path path = Path.of(argument)
                        .toAbsolutePath()
                        .normalize();

                collectJavaFiles(path, javaFiles, errors);
            } catch (InvalidPathException exception) {
                errors.add("Invalid path: " + argument);
            }
        }

        if (!errors.isEmpty()) {
            throw new SourceInputException(errors);
        }

        if (javaFiles.isEmpty()) {
            throw new IllegalArgumentException(
                    "No Java files were found in the provided paths."
            );
        }

        return List.copyOf(javaFiles);
    }

    private void collectJavaFiles(
            Path path,
            Set<Path> javaFiles,
            List<String> errors
    ) {
        if (!Files.exists(path)) {
            errors.add("Path does not exist: " + path);
            return;
        }

        if (!Files.isReadable(path)) {
            errors.add("Path is not readable: " + path);
            return;
        }

        if (Files.isRegularFile(path)) {
            collectSingleFile(path, javaFiles, errors);
            return;
        }

        if (Files.isDirectory(path)) {
            collectDirectory(path, javaFiles, errors);
            return;
        }

        errors.add("Unsupported path type: " + path);
    }

    private void collectSingleFile(
            Path file,
            Set<Path> javaFiles,
            List<String> errors
    ) {
        if (!isJavaFile(file)) {
            errors.add("File is not a Java source file: " + file);
            return;
        }

        javaFiles.add(resolveRealPath(file, errors));
    }

    private void collectDirectory(
            Path directory,
            Set<Path> javaFiles,
            List<String> errors
    ) {
        try (Stream<Path> paths = Files.walk(directory)) {
            paths.filter(Files::isRegularFile)
                    .filter(this::isJavaFile)
                    .map(path -> resolveRealPath(path, errors))
                    .forEach(javaFiles::add);
        } catch (IOException exception) {
            errors.add(
                    "Could not read directory %s: %s"
                            .formatted(directory, exception.getMessage())
            );
        }
    }

    private Path resolveRealPath(Path path, List<String> errors) {
        try {
            return path.toRealPath();
        } catch (IOException exception) {
            errors.add(
                    "Could not resolve path %s: %s"
                            .formatted(path, exception.getMessage())
            );

            return path.toAbsolutePath().normalize();
        }
    }

    private boolean isJavaFile(Path path) {
        Path fileName = path.getFileName();

        return fileName != null
                && fileName.toString().endsWith(".java");
    }
}