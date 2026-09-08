package com.minegolem.zelChatCooldown.model;

import java.time.Duration;

public record CooldownResult(
        boolean allowed,
        Duration remaining
) {

    public static CooldownResult success() {
        return new CooldownResult(true, Duration.ZERO);
    }

    public static CooldownResult denied(Duration remaining) {
        return new CooldownResult(false, remaining);
    }
}