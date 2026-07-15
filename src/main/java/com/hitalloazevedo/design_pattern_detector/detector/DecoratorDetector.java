package com.hitalloazevedo.design_pattern_detector.detector;

import java.util.List;

import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.result.DetectionResult;

public class DecoratorDetector implements DesignPatternDetector {

    @Override
    public List<DetectionResult> detect(ProjectModel project) {
        // Implement the detection logic for the Decorator pattern here
        return List.of(); // Return an empty list for now
    }
    
}
