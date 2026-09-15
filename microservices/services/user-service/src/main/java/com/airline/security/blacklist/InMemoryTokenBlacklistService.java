package com.airline.security.blacklist;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class InMemoryTokenBlacklistService implements TokenBlacklistService {

    private final Map<String, Instant> blacklist = new ConcurrentHashMap<>();

    @Override
    public void blacklist(String token, Instant expiresAt) {
        blacklist.put(token, expiresAt);
        log.debug("Token blacklisted, expires at {}", expiresAt);
    }

    @Override
    public boolean isBlacklisted(String token) {
        Instant expiresAt = blacklist.get(token);
        if (expiresAt == null) {
            return false;
        }

        if (expiresAt.isBefore(Instant.now())) {
            blacklist.remove(token);
            return false;
        }
        return true;
    }

    /**
     * Limpia tokens expirados cada hora.
     */
    @Scheduled(fixedRate = 3600000)
    public void clearExpired() {
        Instant now = Instant.now();
        int before = blacklist.size();
        blacklist.entrySet().removeIf(entry -> entry.getValue().isBefore(now));
        int removed = before - blacklist.size();
        if (removed > 0) {
            log.info("Cleared {} expired tokens from blacklist", removed);
        }
    }

}