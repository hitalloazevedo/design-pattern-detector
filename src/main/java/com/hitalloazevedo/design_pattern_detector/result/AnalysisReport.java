package com.hitalloazevedo.design_pattern_detector.result;

import java.nio.file.Path;
import java.util.List;

import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;

public record AnalysisReport(
        List<Path> analyzedFiles,
        ProjectModel project,
        List<DetectionResult> detections
) {
    public AnalysisReport {
        analyzedFiles = List.copyOf(analyzedFiles);
        detections = List.copyOf(detections);
    }

    public boolean hasDetections() {
        return !detections.isEmpty();
    }
}