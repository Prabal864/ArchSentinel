package com.archsentinel.installation;

import com.archsentinel.repo.RepoClonerConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Stores and resolves GitHub tokens per repository.
 *
 * Strategy: Cache-first, DB-best-effort.
 * - In-memory ConcurrentHashMap is the primary store (always works).
 * - PostgreSQL is secondary (persists across restarts, but app works without it).
 *
 * Token resolution: user PAT (cache/DB) → env GITHUB_TOKEN fallback.
 */
@Component
public class InstallationTokenStore {

    private static final Logger log = LoggerFactory.getLogger(InstallationTokenStore.class);
    private static final String GITHUB_API = "https://api.github.com";

    private final RepoClonerConfig config;
    private final RegisteredTokenRepository tokenRepository;
    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper mapper = new ObjectMapper();

    private final ConcurrentHashMap<String, String> tokenCache = new ConcurrentHashMap<>();
    private boolean dbAvailable = false;

    public InstallationTokenStore(RepoClonerConfig config, RegisteredTokenRepository tokenRepository) {
        this.config = config;
        this.tokenRepository = tokenRepository;
    }

    @PostConstruct
    public void loadFromDatabase() {
        try {
            List<RegisteredToken> all = tokenRepository.findAll();
            for (RegisteredToken rt : all) {
                tokenCache.put(rt.getRepoFullName(), rt.getToken());
            }
            dbAvailable = true;
            log.info("✓ Database connected — loaded {} tokens", all.size());
        } catch (Exception e) {
            dbAvailable = false;
            log.warn("⚠️ Database unavailable — running with in-memory only: {}", e.getMessage());
        }
    }

    // ======================== REGISTER ========================

    public void registerUserToken(String repoFullName, String token, String username) {
        String key = repoFullName.toLowerCase();

        // Always save to cache (never fails)
        tokenCache.put(key, token);
        log.info("✓ Token cached for repo: {} (user: {})", repoFullName, username);

        // Best-effort save to DB
        saveToDb(key, token, username);
    }

    public void registerUserToken(String repoFullName, String token) {
        registerUserToken(repoFullName, token, null);
    }

    public List<String> registerUserTokenAndDiscover(String token, String username) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/vnd.github.v3+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<String> response = restTemplate.exchange(
                    GITHUB_API + "/user/repos?per_page=100&sort=pushed",
                    HttpMethod.GET, entity, String.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode repos = mapper.readTree(response.getBody());
                List<String> registered = new ArrayList<>();
                for (JsonNode repo : repos) {
                    String fullName = repo.path("full_name").asText();
                    if (!fullName.isEmpty()) {
                        registerUserToken(fullName, token, username);
                        registered.add(fullName);
                    }
                }
                log.info("✓ Token registered for {} repos (user: {})", registered.size(), username);
                return registered;
            }
        } catch (Exception e) {
            log.error("Failed to discover repos: {}", e.getMessage());
        }
        return List.of();
    }

    public List<String> registerUserTokenAndDiscover(String token) {
        return registerUserTokenAndDiscover(token, null);
    }

    public void removeUserToken(String repoFullName) {
        String key = repoFullName.toLowerCase();
        tokenCache.remove(key);

        // Best-effort remove from DB
        try {
            tokenRepository.deleteByRepoFullName(key);
        } catch (Exception e) {
            log.debug("DB delete failed for {}: {}", key, e.getMessage());
        }
        log.info("✓ Token removed for repo: {}", repoFullName);
    }

    // ======================== TOKEN RESOLUTION ========================

    public String getTokenForRepo(String repoFullName) {
        String key = repoFullName.toLowerCase();

        // 1. Cache (fast, always available)
        String cached = tokenCache.get(key);
        if (cached != null && !cached.isEmpty()) {
            log.debug("Using cached user token for repo: {}", repoFullName);
            return cached;
        }

        // 2. DB fallback
        try {
            var dbToken = tokenRepository.findByRepoFullName(key);
            if (dbToken.isPresent()) {
                String token = dbToken.get().getToken();
                tokenCache.put(key, token);
                log.debug("Using DB token for repo: {}", repoFullName);
                return token;
            }
        } catch (Exception e) {
            log.debug("DB lookup failed for {}: {}", key, e.getMessage());
        }

        // 3. Env fallback
        String envToken = config.getGithubToken();
        if (envToken != null && !envToken.isEmpty()) {
            log.debug("Using env GITHUB_TOKEN fallback for repo: {}", repoFullName);
            return envToken;
        }

        log.warn("⚠️ No token available for repo '{}' — register a token at POST /api/register-token", repoFullName);
        return null;
    }

    public boolean hasToken(String repoFullName) {
        return getTokenForRepo(repoFullName) != null;
    }

    public Map<String, String> getRegisteredRepos() {
        Map<String, String> result = new LinkedHashMap<>();

        // Try DB first for full info (username + masked token)
        try {
            List<RegisteredToken> all = tokenRepository.findAll();
            for (RegisteredToken rt : all) {
                String display = rt.getGithubUsername() != null
                        ? rt.getGithubUsername() + " — " + rt.getMaskedToken()
                        : rt.getMaskedToken();
                result.put(rt.getRepoFullName(), display);
            }
            return result;
        } catch (Exception e) {
            // DB down — fall back to cache
        }

        // Fallback: show from cache
        tokenCache.forEach((repo, token) -> {
            String masked = token.length() > 8
                    ? token.substring(0, 4) + "****" + token.substring(token.length() - 4)
                    : "****";
            result.put(repo, masked);
        });
        return result;
    }

    // ======================== Internal ========================

    private void saveToDb(String repoFullName, String token, String username) {
        try {
            RegisteredToken entity = tokenRepository.findByRepoFullName(repoFullName)
                    .orElse(new RegisteredToken(repoFullName, token, username));
            entity.setToken(token);
            entity.setGithubUsername(username);
            tokenRepository.save(entity);
            dbAvailable = true;
        } catch (Exception e) {
            dbAvailable = false;
            log.warn("⚠️ Could not save to DB (token is cached in memory): {}", e.getMessage());
        }
    }
}

