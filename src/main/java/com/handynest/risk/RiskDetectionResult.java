package com.handynest.risk;

public record RiskDetectionResult(
    RiskType riskType, RiskSeverity severity, String detectedText, String normalizedDetectedValue) {
  public boolean highRisk() {
    return severity == RiskSeverity.HIGH || severity == RiskSeverity.CRITICAL;
  }
}
