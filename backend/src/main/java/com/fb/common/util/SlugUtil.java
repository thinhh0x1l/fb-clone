package com.fb.common.util;

import java.text.Normalizer;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

public final class SlugUtil {

    private SlugUtil() {
    }

    private static final Pattern NON_LATIN = Pattern.compile("[^\\w-]");
    private static final Pattern WHITESPACE = Pattern.compile("\\s+");
    private static final Pattern ACCENT_PATTERN = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
    private static final Pattern MULTIPLE_DASHES = Pattern.compile("-{2,}");

    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "";
        }
        String normalized = Normalizer.normalize(input, Normalizer.Form.NFD);
        String withoutAccents = ACCENT_PATTERN.matcher(normalized).replaceAll("");
        String slug = WHITESPACE.matcher(withoutAccents).replaceAll("-");
        slug = NON_LATIN.matcher(slug).replaceAll("-");
        slug = slug.toLowerCase(Locale.ROOT);
        slug = MULTIPLE_DASHES.matcher(slug).replaceAll("-");
        slug = slug.replaceAll("^-|-$", "");
        return slug;
    }

    public static String generateUniqueSlug(String base) {
        String slug = toSlug(base);
        if (slug.isEmpty()) {
            slug = "item";
        }
        String shortId = UUID.randomUUID().toString().substring(0, 8);
        return slug + "-" + shortId;
    }

    public static String toUrlFriendly(String input) {
        return toSlug(input);
    }

    public static String trimSlug(String slug, int maxLength) {
        if (slug == null || slug.length() <= maxLength) {
            return slug;
        }
        String trimmed = slug.substring(0, maxLength);
        int lastDash = trimmed.lastIndexOf('-');
        if (lastDash > 0) {
            trimmed = trimmed.substring(0, lastDash);
        }
        return trimmed;
    }
}
