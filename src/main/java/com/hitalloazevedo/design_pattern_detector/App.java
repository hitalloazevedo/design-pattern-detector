package com.hitalloazevedo.design_pattern_detector;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import com.github.javaparser.ast.CompilationUnit;
import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.parser.JavaSourceParser;
import com.hitalloazevedo.design_pattern_detector.parser.ParsedSource;
import com.hitalloazevedo.design_pattern_detector.parser.ProjectModelExtractor;
import com.hitalloazevedo.design_pattern_detector.parser.SourceInputHandler;

public class App {
  public static void main(String[] args) {
    JavaSourceParser parser = new JavaSourceParser();
    SourceInputHandler inputHandler = new SourceInputHandler();

    try {
      List<Path> javaFiles = inputHandler.resolve(args);
      List<ParsedSource> parsedSources = new ArrayList<>();

      for (Path javaFile : javaFiles) {
        CompilationUnit compilationUnit = parser.parse(javaFile);

        parsedSources.add(
            new ParsedSource(
                javaFile,
                compilationUnit));
      }

      ProjectModelExtractor extractor = new ProjectModelExtractor();

      ProjectModel project = extractor.extract(parsedSources);

      System.out.println(project);

    } catch (IllegalArgumentException exception) {
      System.err.println(exception.getMessage());
      printUsage();
      System.exit(1);
    } catch (IOException exception) {
      System.err.println("Erro ao ler o arquivo: " + exception.getMessage());
      System.exit(1);
    }
  }

  private static void printUsage() {
    System.err.println("""
        Usage:
          java -jar detector.jar <path> [additional paths...]

        Accepted inputs:
          - A single .java file
          - Multiple .java files
          - One or more directories containing .java files

        Examples:
          java -jar detector.jar Example.java
          java -jar detector.jar A.java B.java C.java
          java -jar detector.jar ./src/main/java
          java -jar detector.jar A.java ./src/main/java
        """);
  }
}
