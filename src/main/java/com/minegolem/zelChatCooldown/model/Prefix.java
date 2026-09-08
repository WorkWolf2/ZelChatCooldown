package com.minegolem.zelChatCooldown.model;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

public class Prefix {

    private final Pattern pattern;

    public Prefix(List<String> prefixes) {
        String joined = prefixes.stream()
                .map(Pattern::quote)
                .reduce((a, b) -> a + "|" + b)
                .orElse("");

        this.pattern = Pattern.compile("(" + joined + ")", Pattern.CASE_INSENSITIVE);
    }

    public String findPrefix(Component component) {
        if (component == null) return null;

        String plain = PlainTextComponentSerializer.plainText().serialize(component);
        Matcher matcher = pattern.matcher(plain);

        return matcher.find() ? matcher.group(1) : null;
    }

    public boolean containsPrefix(Component component) {
        return findPrefix(component) != null;
    }
}

