package com.booksauthors.application;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Locale;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Нормалізація пошукового запиту перед кешуванням: нижній регістр, прибирання розділових знаків,
 * унікальні слова, сортування за алфавітом. «Energy green» і «green, energy!» дають однаковий ключ.
 */
@Component
public class QueryNormalizer {

    private static final Pattern SPLIT = Pattern.compile("[^\\p{L}\\p{N}]+");

    public String normalize(String rawQuery) {
        if (rawQuery == null) return "";
        return Arrays.stream(SPLIT.split(rawQuery.toLowerCase(Locale.ROOT).trim()))
                .filter(s -> !s.isEmpty())
                .distinct()
                .sorted()
                .collect(Collectors.joining(" "));
    }
}
