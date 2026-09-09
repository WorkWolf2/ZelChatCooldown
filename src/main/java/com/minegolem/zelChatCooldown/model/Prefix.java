package com.minegolem.zelChatCooldown.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.minegolem.zelChatCooldown.ZelChatCooldown.INSTANCE;

public class Prefix {

    private final Pattern pattern;

    public Prefix(List<String> prefixes) {

        if (prefixes == null || prefixes.isEmpty()) {
            this.pattern = null;

            INSTANCE.debug("Prefix initialized with no prefixes.");

            return;
        }

        String joined = prefixes.stream()
                .filter(prefix -> prefix != null && !prefix.isBlank())
                .map(Pattern::quote)
                .reduce((a, b) -> a + "|" + b)
                .orElse("");

        if (joined.isEmpty()) {
            this.pattern = null;

            INSTANCE.debug("Prefix initialized with no valid prefixes.");

            return;
        }

        this.pattern = Pattern.compile("(" + joined + ")", Pattern.CASE_INSENSITIVE);

        INSTANCE.debug("Prefix initialized. Prefixes: " + prefixes);
    }

    public String findPrefix(Component component) {

        if (component == null) {
            INSTANCE.debug("findPrefix() called with null component.");
            return null;
        }

        if (pattern == null) {
            return null;
        }

        String plain = PlainTextComponentSerializer.plainText().serialize(component);

        INSTANCE.debug("Searching prefix in component: \"" + plain + "\"");

        Matcher matcher = pattern.matcher(plain);

        if (!matcher.find()) {
            INSTANCE.debug("No prefix found in component.");
            return null;
        }

        String prefix = matcher.group(1);

        INSTANCE.debug("Prefix found: \"" + prefix + "\"");

        return prefix;
    }

    public boolean containsPrefix(Component component) {

        boolean contains = findPrefix(component) != null;

        INSTANCE.debug("containsPrefix() result: " + contains);

        return contains;
    }
}