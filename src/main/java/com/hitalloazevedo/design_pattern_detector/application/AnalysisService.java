package com.hitalloazevedo.design_pattern_detector.application;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import com.github.javaparser.ast.CompilationUnit;
import com.hitalloazevedo.design_pattern_detector.detector.DesignPatternDetector;
import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.parser.JavaSourceParser;
import com.hitalloazevedo.design_pattern_detector.parser.ParsedSource;
import com.hitalloazevedo.design_pattern_detector.parser.ProjectModelExtractor;
import com.hitalloazevedo.design_pattern_detector.parser.SourceInput;
import com.hitalloazevedo.design_pattern_detector.parser.SourceInputHandler;
import com.hitalloazevedo.design_pattern_detector.parser.SourceRootResolver;
import com.hitalloazevedo.design_pattern_detector.result.AnalysisReport;
import com.hitalloazevedo.design_pattern_detector.result.DetectionResult;

public final class AnalysisService {

        private final SourceInputHandler inputHandler;
        private final SourceRootResolver sourceRootResolver;
        private final ProjectModelExtractor extractor;
        private final List<DesignPatternDetector> detectors;

        public AnalysisService(
                        SourceInputHandler inputHandler,
                        SourceRootResolver sourceRootResolver,
                        ProjectModelExtractor extractor,
                        List<DesignPatternDetector> detectors) {
                this.inputHandler = Objects.requireNonNull(
                                inputHandler,
                                "inputHandler cannot be null");

                this.sourceRootResolver = Objects.requireNonNull(
                                sourceRootResolver,
                                "sourceRootResolver cannot be null");

                this.extractor = Objects.requireNonNull(
                                extractor,
                                "extractor cannot be null");

                this.detectors = List.copyOf(
                                Objects.requireNonNull(
                                                detectors,
                                                "detectors cannot be null"));
        }

        public AnalysisReport analyze(String[] arguments)
                        throws IOException {

                SourceInput input = inputHandler.resolve(arguments);

                Set<Path> sourceRoots = sourceRootResolver.resolve(input);

                JavaSourceParser parser = new JavaSourceParser(sourceRoots);

                List<ParsedSource> parsedSources = parseFiles(
                                input.javaFiles(),
                                parser);

                ProjectModel project = extractor.extract(parsedSources);

                List<DetectionResult> detections = runDetectors(project);

                System.out.println("SOURCE ROOTS");

                sourceRoots.forEach(root ->
                        System.out.println("- " + root)
);

                return new AnalysisReport(
                                input.javaFiles(),
                                project,
                                detections);
        }

        private List<ParsedSource> parseFiles(
                        List<Path> javaFiles,
                        JavaSourceParser parser) throws IOException {

                List<ParsedSource> sources = new ArrayList<>();

                for (Path javaFile : javaFiles) {
                        CompilationUnit compilationUnit = parser.parse(javaFile);

                        sources.add(
                                        new ParsedSource(
                                                        javaFile,
                                                        compilationUnit));
                }

                return List.copyOf(sources);
        }

        private List<DetectionResult> runDetectors(
                        ProjectModel project) {
                return detectors.stream()
                                .flatMap(detector -> detector.detect(project).stream())
                                .toList();
        }
}