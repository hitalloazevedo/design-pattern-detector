package com.hitalloazevedo.design_pattern_detector.result;

import com.hitalloazevedo.design_pattern_detector.model.SourceLocation;

public record DetectionEvidence(
        String description,
        SourceLocation location
) {
}