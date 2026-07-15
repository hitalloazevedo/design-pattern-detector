package com.hitalloazevedo.design_pattern_detector;

import java.io.IOException;
import com.hitalloazevedo.design_pattern_detector.application.AnalysisService;
import com.hitalloazevedo.design_pattern_detector.application.ApplicationFactory;
import com.hitalloazevedo.design_pattern_detector.debug.ProjectModelPrinter;
import com.hitalloazevedo.design_pattern_detector.parser.JavaSourceParseException;
import com.hitalloazevedo.design_pattern_detector.parser.SourceInputException;
import com.hitalloazevedo.design_pattern_detector.report.ConsoleReportGenerator;
import com.hitalloazevedo.design_pattern_detector.report.ReportGenerator;
import com.hitalloazevedo.design_pattern_detector.result.AnalysisReport;

public class App {
  public static void main(String[] args) {
    try {
      AnalysisService service = ApplicationFactory.createAnalysisService();

      AnalysisReport report = service.analyze(args);

      ProjectModelPrinter printer = new ProjectModelPrinter();

      System.out.println(
          printer.print(report.project()));

      ReportGenerator reportGenerator = new ConsoleReportGenerator();

      System.out.println(
          reportGenerator.generate(report));
    } catch (SourceInputException exception) {
      System.err.println(exception.getMessage());
      printUsage();
      System.exit(1);
    } catch (JavaSourceParseException exception) {
      System.err.println(exception.getMessage());
      System.exit(1);
    } catch (IOException exception) {
      System.err.println(
          "Erro ao ler os arquivos: "
              + exception.getMessage());
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
