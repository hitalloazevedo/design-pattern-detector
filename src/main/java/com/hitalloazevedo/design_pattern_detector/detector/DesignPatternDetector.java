package com.hitalloazevedo.design_pattern_detector.detector;

import java.util.List;

import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.result.DetectionResult;

public interface DesignPatternDetector {

    List<DetectionResult> detect(ProjectModel project);
}