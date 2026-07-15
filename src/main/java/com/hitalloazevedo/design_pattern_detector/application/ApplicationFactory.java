package com.hitalloazevedo.design_pattern_detector.application;

import java.util.List;

import com.hitalloazevedo.design_pattern_detector.detector.AdapterDetector;
import com.hitalloazevedo.design_pattern_detector.detector.DecoratorDetector;
import com.hitalloazevedo.design_pattern_detector.detector.StrategyDetector;
import com.hitalloazevedo.design_pattern_detector.parser.ProjectModelExtractor;
import com.hitalloazevedo.design_pattern_detector.parser.SourceInputHandler;

public final class ApplicationFactory {

    private ApplicationFactory() {
    }

    public static AnalysisService createAnalysisService() {
        return new AnalysisService(
                new SourceInputHandler(),
                new ProjectModelExtractor(),
                List.of(
                        new DecoratorDetector(),
                        new AdapterDetector(),
                        new StrategyDetector()
                )
        );
    }
}