package com.openmosque.modules.community.service;

import com.openmosque.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

@Slf4j
@Service
public class ProfanityFilterService {

    // Standard baseline profanity and toxic terms
    private static final Set<String> BLOCKED_WORDS = new HashSet<>(Arrays.asList(
            "abuse", "asshole", "bastard", "bitch", "crap", "damn", "dick",
            "fuck", "idiot", "nigger", "piss", "pussy", "shit", "slut", "whore"
    ));

    private static final Pattern SPAM_LINK_PATTERN = Pattern.compile("(?i)\\b(https?://|www\\.)[a-z0-9.\\-]+(/\\S*)?");

    public boolean containsProfanity(String text) {
        if (text == null || text.isBlank()) {
            return false;
        }

        String normalized = text.toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9\\s]", " ");

        String[] words = normalized.split("\\s+");
        for (String word : words) {
            if (BLOCKED_WORDS.contains(word)) {
                return true;
            }
        }
        return false;
    }

    public void validateCleanContent(String text, String fieldName) {
        if (text == null || text.isBlank()) {
            return;
        }

        if (containsProfanity(text)) {
            log.warn("Profanity filter triggered for field '{}'", fieldName);
            throw new BadRequestException("Content contains prohibited or offensive language.");
        }

        // Basic spam check: prevent excessive external URL spamming in comments
        long urlCount = SPAM_LINK_PATTERN.matcher(text).results().count();
        if (urlCount > 2) {
            log.warn("Spam link detector triggered on field '{}' (urlCount: {})", fieldName, urlCount);
            throw new BadRequestException("Content contains too many external links or suspicious URLs.");
        }
    }
}
