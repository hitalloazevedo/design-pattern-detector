package com.hitalloazevedo.design_pattern_detector.result;

import java.util.List;

public record DetectionResult(
        PatternType pattern,
        String mainType,
        List<DetectionEvidence> evidence,
        String advantage,
        String disadvantage
) {
    public DetectionResult {
        evidence = List.copyOf(evidence);
    }
}