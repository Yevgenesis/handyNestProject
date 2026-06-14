package com.handynest.risk;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Service;

@Service
public class RiskDetectionService {

  private static final Pattern PAYMENT_OUTSIDE_PLATFORM_PATTERN =
      Pattern.compile(
          "(?iu)(оплат[ауы]?\\s+вне\\s+платформы|перевед[иі]|на\\s+карту|kaspi|каспи|"
              + "platformadan\\s+tashqari|kartaga\\s+o['’]?tkaz|payme|click)");
  private static final Pattern EXTERNAL_MESSENGER_PATTERN =
      Pattern.compile(
          "(?iu)(telegram|whatsapp|wa\\.me|t\\.me|instagram|\\bватсап\\b|\\bтелеграм\\b|"
              + "\\btelegramda\\b|\\bwhatsappda\\b|\\blichkaga\\b|\\bshaxsiyga\\b)");
  private static final Pattern PHONE_PATTERN =
      Pattern.compile(
          "(?iu)(?<!\\d)(?:(?:\\+|00)?998|8)?[\\s\\-().]*\\d{2}[\\s\\-().]*\\d{3}"
              + "[\\s\\-().]*\\d{2}[\\s\\-().]*\\d{2}(?!\\d)|"
              + "(?<!\\d)(?:\\+?7|8)?[\\s\\-().]*\\d{3}[\\s\\-().]*\\d{3}"
              + "[\\s\\-().]*\\d{2}[\\s\\-().]*\\d{2}(?!\\d)");
  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("(?iu)[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}");
  private static final Pattern EXTERNAL_LINK_PATTERN =
      Pattern.compile("(?iu)(?:https?://|www\\.)\\S+");

  public boolean hasRisk(String text) {
    return detect(text).isPresent();
  }

  public Optional<RiskDetectionResult> detect(String text) {
    if (text == null || text.isBlank()) {
      return Optional.empty();
    }
    return firstMatch(
            text,
            PAYMENT_OUTSIDE_PLATFORM_PATTERN,
            RiskType.PAYMENT_OUTSIDE_PLATFORM,
            RiskSeverity.HIGH)
        .or(
            () ->
                firstMatch(
                    text,
                    EXTERNAL_MESSENGER_PATTERN,
                    RiskType.EXTERNAL_MESSENGER,
                    RiskSeverity.HIGH))
        .or(() -> firstMatch(text, PHONE_PATTERN, RiskType.PHONE_SHARED, RiskSeverity.MEDIUM))
        .or(() -> firstMatch(text, EMAIL_PATTERN, RiskType.EMAIL_SHARED, RiskSeverity.MEDIUM))
        .or(
            () ->
                firstMatch(
                    text, EXTERNAL_LINK_PATTERN, RiskType.EXTERNAL_LINK, RiskSeverity.MEDIUM));
  }

  private Optional<RiskDetectionResult> firstMatch(
      String text, Pattern pattern, RiskType riskType, RiskSeverity severity) {
    Matcher matcher = pattern.matcher(text);
    if (!matcher.find()) {
      return Optional.empty();
    }
    String detectedText = matcher.group();
    return Optional.of(
        new RiskDetectionResult(riskType, severity, detectedText, normalize(detectedText)));
  }

  private String normalize(String value) {
    return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", " ");
  }
}
