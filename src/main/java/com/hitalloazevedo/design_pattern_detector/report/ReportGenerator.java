package com.hitalloazevedo.design_pattern_detector.report;

import com.hitalloazevedo.design_pattern_detector.result.AnalysisReport;

public interface ReportGenerator {

    String generate(AnalysisReport report);
}