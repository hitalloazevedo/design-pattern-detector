package com.hitalloazevedo.design_pattern_detector.report;

import com.hitalloazevedo.design_pattern_detector.result.AnalysisReport;
import com.hitalloazevedo.design_pattern_detector.result.DetectionEvidence;
import com.hitalloazevedo.design_pattern_detector.result.DetectionResult;

public final class ConsoleReportGenerator
        implements ReportGenerator {

    @Override
    public String generate(AnalysisReport report) {
        if (!report.hasDetections()) {
            return "Nenhum padrão foi detectado.";
        }

        StringBuilder output = new StringBuilder();

        for (DetectionResult result : report.detections()) {
            appendDetection(output, result);
        }

        return output.toString();
    }

    private void appendDetection(
            StringBuilder output,
            DetectionResult result
    ) {
        output.append("[PADRÃO DETECTADO] ")
                .append(result.pattern())
                .append(System.lineSeparator());

        output.append("Elementos identificados:")
                .append(System.lineSeparator());

        for (DetectionEvidence evidence : result.evidence()) {
            output.append("- ")
                    .append(evidence.description())
                    .append(" — ")
                    .append(evidence.location().sourceFile())
                    .append(":")
                    .append(evidence.location().beginLine())
                    .append(System.lineSeparator());
        }

        output.append("Vantagem neste contexto: ")
                .append(result.advantage())
                .append(System.lineSeparator());

        output.append("Risco/desvantagem neste contexto: ")
                .append(result.disadvantage())
                .append(System.lineSeparator())
                .append(System.lineSeparator());
    }
}