package com.hitalloazevedo.design_pattern_detector.detector;

import java.util.List;

import com.hitalloazevedo.design_pattern_detector.model.ProjectModel;
import com.hitalloazevedo.design_pattern_detector.result.DetectionResult;

public class AdapterDetector implements DesignPatternDetector {

    @Override
    public List<DetectionResult> detect(ProjectModel project) {
        // Implement the detection logic for the Adapter pattern here
        return List.of(); // Return an empty list for now
    }
    
}
