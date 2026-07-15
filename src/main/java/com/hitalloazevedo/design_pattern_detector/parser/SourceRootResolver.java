package com.hitalloazevedo.design_pattern_detector.parser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.ast.CompilationUnit;

public final class SourceRootResolver {

        private final JavaParser syntaxParser;

        public SourceRootResolver() {
                ParserConfiguration configuration = new ParserConfiguration()
                                .setLanguageLevel(
                                                ParserConfiguration.LanguageLevel.JAVA_22);

                this.syntaxParser = new JavaParser(configuration);
        }

        public Set<Path> resolve(SourceInput input)
                        throws IOException {

                Objects.requireNonNull(
                                input,
                                "input cannot be null");

                Set<Path> sourceRoots = new LinkedHashSet<>();

                addProvidedDirectories(
                                input,
                                sourceRoots);

                addInferredFileRoots(
                                input,
                                sourceRoots);

                if (sourceRoots.isEmpty()) {
                        throw new IllegalArgumentException(
                                        "Could not determine any Java source root.");
                }

                Set<Path> cleanedRoots = removeRedundantRoots(sourceRoots);

                return Set.copyOf(cleanedRoots);
        }

        private Set<Path> removeRedundantRoots(
                        Set<Path> roots) {
                Set<Path> cleaned = new LinkedHashSet<>();

                for (Path candidate : roots) {
                        boolean coveredByAnotherRoot = roots.stream()
                                        .filter(other -> !other.equals(candidate))
                                        .anyMatch(candidate::startsWith);

                        if (!coveredByAnotherRoot) {
                                cleaned.add(candidate);
                        }
                }

                return cleaned;
        }

        private void addProvidedDirectories(
                        SourceInput input,
                        Set<Path> sourceRoots) {
                for (Path directory : input.providedDirectories()) {

                        Path normalizedDirectory = normalizeDirectory(directory);

                        sourceRoots.add(normalizedDirectory);
                }
        }

        private void addInferredFileRoots(
                        SourceInput input,
                        Set<Path> sourceRoots) throws IOException {

                for (Path javaFile : input.javaFiles()) {
                        sourceRoots.add(
                                        inferSourceRoot(javaFile));
                }
        }

        private Path inferSourceRoot(Path javaFile)
                        throws IOException {

                Objects.requireNonNull(
                                javaFile,
                                "javaFile cannot be null");

                Path normalizedFile = javaFile
                                .toAbsolutePath()
                                .normalize();

                if (!Files.isRegularFile(normalizedFile)) {
                        throw new IllegalArgumentException(
                                        "Java source file does not exist: "
                                                        + normalizedFile);
                }

                CompilationUnit compilationUnit = parseForPackage(normalizedFile);

                Path parentDirectory = normalizedFile.getParent();

                if (parentDirectory == null) {
                        throw new IllegalArgumentException(
                                        "Could not determine the parent directory of: "
                                                        + normalizedFile);
                }

                String packageName = compilationUnit
                                .getPackageDeclaration()
                                .map(declaration -> declaration.getNameAsString())
                                .orElse("");

                if (packageName.isBlank()) {
                        return parentDirectory;
                }

                return moveAbovePackageHierarchy(
                                normalizedFile,
                                parentDirectory,
                                packageName);
        }

        private CompilationUnit parseForPackage(
                        Path javaFile) throws IOException {

                ParseResult<CompilationUnit> result = syntaxParser.parse(javaFile);

                if (result.isSuccessful()
                                && result.getResult().isPresent()) {
                        return result.getResult().get();
                }

                String problems = result.getProblems()
                                .stream()
                                .map(Object::toString)
                                .reduce(
                                                "",
                                                (current, problem) -> current
                                                                + System.lineSeparator()
                                                                + problem);

                throw new IllegalArgumentException(
                                "Could not read package declaration from: "
                                                + javaFile
                                                + problems);
        }

        private Path moveAbovePackageHierarchy(
                        Path javaFile,
                        Path initialDirectory,
                        String packageName) {
                Path current = initialDirectory;

                String[] packageSegments = packageName.split("\\.");

                for (int index = packageSegments.length - 1; index >= 0; index--) {

                        Path directoryName = current.getFileName();

                        if (directoryName == null
                                        || !directoryName.toString()
                                                        .equals(packageSegments[index])) {

                                throw new IllegalArgumentException(
                                                "The package declaration '%s' does not match "
                                                                + "the file location: %s"
                                                                                .formatted(
                                                                                                packageName,
                                                                                                javaFile));
                        }

                        Path parent = current.getParent();

                        if (parent == null) {
                                throw new IllegalArgumentException(
                                                "Could not infer the source root for: "
                                                                + javaFile);
                        }

                        current = parent;
                }

                return current;
        }

        private Path normalizeDirectory(Path directory) {
                Objects.requireNonNull(
                                directory,
                                "directory cannot be null");

                Path normalizedDirectory = directory
                                .toAbsolutePath()
                                .normalize();

                if (!Files.isDirectory(normalizedDirectory)) {
                        throw new IllegalArgumentException(
                                        "Source directory does not exist: "
                                                        + normalizedDirectory);
                }

                return normalizedDirectory;
        }
}