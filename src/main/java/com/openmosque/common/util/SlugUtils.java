package com.openmosque.common.util;

import java.text.Normalizer;
import java.util.function.Predicate;

/**
 * <h3>SlugUtils</h3>
 * <p>
 * <b>WHY THIS IS WRITTEN:</b><br>
 * Provides centralized, standardized URL-slug generation and normalization across the application.
 * Eliminates duplicate slug-building code between manual mosque creation, crowdsourced submissions,
 * and automated OpenStreetMap ingestion. Handles international diacritics, accents, whitespace collapsing,
 * and uniqueness verification against the database.
 * </p>
 * <p>
 * <b>WHERE IT IS USED:</b><br>
 * <ul>
 *   <li>{@link com.openmosque.modules.mosque.service.MosqueService#createMosque}</li>
 *   <li>{@link com.openmosque.modules.moderation.service.ModerationService#approveSubmission}</li>
 *   <li>{@link com.openmosque.modules.ingestion.service.OsmIngestionService}</li>
 * </ul>
 * </p>
 */
public final class SlugUtils {

    private SlugUtils() {
        // Prevent instantiation of utility class
    }

    /**
     * Converts raw text into a clean, URL-friendly slug.
     * <p>
     * Performs Unicode NFD normalization to remove accents/diacritics (e.g. "Jāmi'ah" -> "jamiah"),
     * converts to lowercase, removes non-alphanumeric characters, and collapses whitespace into single hyphens.
     * </p>
     *
     * @param input Raw text to convert (e.g., "East London Central Mosque")
     * @return Normalized URL slug (e.g., "east-london-central-mosque")
     */
    public static String toSlug(String input) {
        if (input == null || input.isBlank()) {
            return "mosque";
        }
        String normalized = Normalizer.normalize(input.trim().toLowerCase(), Normalizer.Form.NFD)
                .replaceAll("\\p{InCombiningDiacriticalMarks}+", "")
                .replaceAll("[^a-z0-9\\s-]", "")
                .trim()
                .replaceAll("\\s+", "-")
                .replaceAll("-{2,}", "-");

        return normalized.isEmpty() ? "mosque" : normalized;
    }

    /**
     * Combines name and city into a normalized URL slug.
     *
     * @param name Mosque name (e.g., "Al-Noor Mosque")
     * @param city City name (e.g., "Manchester")
     * @return Normalized slug (e.g., "al-noor-mosque-manchester")
     */
    public static String toSlug(String name, String city) {
        String combined = (name != null ? name : "mosque") +
                (city != null && !city.isBlank() ? " " + city : "");
        return toSlug(combined);
    }

    /**
     * Generates a guaranteed unique slug by appending an incrementing integer suffix
     * if a collision is detected by the supplied existence checker.
     *
     * @param name Mosque name
     * @param city Mosque city
     * @param existsChecker Predicate checking whether a slug is already taken (e.g., repository::existsBySlug)
     * @return Guaranteed unique slug string
     */
    public static String generateUniqueSlug(String name, String city, Predicate<String> existsChecker) {
        String baseSlug = toSlug(name, city);
        String candidate = baseSlug;
        int counter = 1;

        while (existsChecker.test(candidate)) {
            candidate = baseSlug + "-" + counter++;
        }
        return candidate;
    }
}
