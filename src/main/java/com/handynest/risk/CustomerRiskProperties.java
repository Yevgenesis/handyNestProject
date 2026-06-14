package com.handynest.risk;

import java.math.BigDecimal;
import java.time.Duration;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.risk.customer")
public class CustomerRiskProperties {

  private BigDecimal lowSeverityPoints = new BigDecimal("1.00");
  private BigDecimal mediumSeverityPoints = new BigDecimal("5.00");
  private BigDecimal highSeverityPoints = new BigDecimal("15.00");
  private BigDecimal criticalSeverityPoints = new BigDecimal("30.00");
  private BigDecimal confirmedComplaintPoints = new BigDecimal("10.00");
  private BigDecimal moderationThreshold = new BigDecimal("30.00");
  private BigDecimal taskCreationBlockThreshold = new BigDecimal("60.00");
  private BigDecimal highValueTaskThreshold = new BigDecimal("5000000.00");
  private int maxActiveTasks = 10;
  private int cancellationEventThreshold = 3;
  private int highCancellationEventThreshold = 5;
  private Duration cancellationWindow = Duration.ofDays(30);
}
