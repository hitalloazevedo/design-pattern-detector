package com.hitalloazevedo.design_pattern_detector;

import java.nio.file.Path;
import java.util.List;

// import com.github.javaparser.ast.CompilationUnit;
// import com.hitalloazevedo.design_pattern_detector.parser.JavaSourceParser;
import com.hitalloazevedo.design_pattern_detector.parser.SourceInputHandler;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) {
        // JavaSourceParser parser = new JavaSourceParser();
        SourceInputHandler inputHandler = new SourceInputHandler();

        try {
            List<Path> javaFiles = inputHandler.resolve(args);
            System.out.println("Arquivos Java encontrados:");
            javaFiles.forEach(System.out::println);
            // for (Path javaFile : javaFiles) {
            //     CompilationUnit cu = parser.parse(javaFile);
            //     System.out.println("Arquivo analisado com sucesso!");
            //     System.out.println(cu.toString());
            // }
        } catch (IllegalArgumentException exception) {
            System.err.println(exception.getMessage());
            printUsage();
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
