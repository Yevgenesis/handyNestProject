package com.handynest.common.i18n;

import java.util.List;
import java.util.Locale;

public final class SupportedLocaleResolver {

    private static final String DEFAULT_LOCALE = "ru";
    private static final List<String> SUPPORTED_LOCALES = List.of("ru", "kk", "en");

    private SupportedLocaleResolver() {
    }

    public static String resolve(String requestedLocale, String acceptLanguageHeader) {
        String queryLocale = normalize(requestedLocale);
        if (queryLocale != null) {
            return queryLocale;
        }

        if (acceptLanguageHeader == null || acceptLanguageHeader.isBlank()) {
            return DEFAULT_LOCALE;
        }

        try {
            for (Locale.LanguageRange languageRange : Locale.LanguageRange.parse(acceptLanguageHeader)) {
                String locale = normalize(languageRange.getRange());
                if (locale != null) {
                    return locale;
                }
            }
        } catch (IllegalArgumentException ignored) {
            return DEFAULT_LOCALE;
        }

        return DEFAULT_LOCALE;
    }

    private static String normalize(String locale) {
        if (locale == null || locale.isBlank()) {
            return null;
        }

        String language = locale.trim()
                .replace('_', '-')
                .split("-", 2)[0]
                .toLowerCase(Locale.ROOT);

        return SUPPORTED_LOCALES.contains(language) ? language : null;
    }
}
