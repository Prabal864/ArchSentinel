package com.archsentinel.installation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * REST API for users to register their GitHub tokens and manage repos.
 * This replaces the need for a single hardcoded GITHUB_TOKEN.
 */
@RestController
@RequestMapping("/api")
public class InstallationController {

    private static final Logger log = LoggerFactory.getLogger(InstallationController.class);
    private final InstallationTokenStore tokenStore;
    private final RestTemplate restTemplate = new RestTemplate();

    public InstallationController(InstallationTokenStore tokenStore) {
        this.tokenStore = tokenStore;
    }

    /**
     * POST /api/register
     * Register a GitHub Personal Access Token.
     * Body: { "token": "ghp_xxx", "repo": "owner/repo" }
     *   - If "repo" is provided: registers for that specific repo (after validating token)
     *   - If "repo" is omitted: auto-discovers all repos the token has access to
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerToken(@RequestBody Map<String, String> body) {
        String token = body.get("token");
        String repo = body.get("repo");

        if (token == null || token.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Token is required"));
        }

        // Validate token by calling GitHub API
        String username;
        try {
            username = validateGitHubToken(token);
        } catch (Exception e) {
            log.error("Token validation error: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Could not validate token: " + e.getMessage()
            ));
        }

        if (username == null) {
            return ResponseEntity.badRequest().body(Map.of(
                    "error", "Invalid token. Make sure it has 'repo' scope and is not expired."
            ));
        }

        try {
            if (repo != null && !repo.isBlank()) {
                tokenStore.registerUserToken(repo, token, username);
                log.info("✓ Token registered for repo: {} (user: {})", repo, username);
                return ResponseEntity.ok(Map.of(
                        "message", "Token registered successfully",
                        "user", username,
                        "repo", repo
                ));
            } else {
                List<String> repos = tokenStore.registerUserTokenAndDiscover(token, username);
                if (repos.isEmpty()) {
                    return ResponseEntity.badRequest().body(Map.of(
                            "error", "Token is valid for user '" + username + "' but no repos found. Check 'repo' scope."
                    ));
                }
                log.info("✓ Token registered for {} repos (user: {})", repos.size(), username);
                return ResponseEntity.ok(Map.of(
                        "message", "Connected as " + username + " — " + repos.size() + " repos registered",
                        "user", username,
                        "repos", repos
                ));
            }
        } catch (Exception e) {
            log.error("❌ Failed to save token: {}", e.getMessage(), e);
            return ResponseEntity.status(500).body(Map.of(
                    "error", "Token is valid but failed to save: " + e.getMessage()
            ));
        }
    }

    /**
     * DELETE /api/register/{owner}/{repo}
     * Remove a registered token for a repo.
     */
    @DeleteMapping("/register/{owner}/{repo}")
    public ResponseEntity<?> removeToken(@PathVariable String owner, @PathVariable String repo) {
        String repoFullName = owner + "/" + repo;
        tokenStore.removeUserToken(repoFullName);
        return ResponseEntity.ok(Map.of("message", "Token removed for " + repoFullName));
    }

    /**
     * GET /api/repos
     * List all registered repos (with masked tokens).
     */
    @GetMapping("/repos")
    public ResponseEntity<?> listRepos() {
        try {
            return ResponseEntity.ok(tokenStore.getRegisteredRepos());
        } catch (Exception e) {
            log.error("Failed to list repos: {}", e.getMessage());
            return ResponseEntity.ok(Map.of());
        }
    }

    /**
     * GET /api/health
     * Check if the system is running and show stats.
     */
    @GetMapping("/health")
    public ResponseEntity<?> health() {
        try {
            return ResponseEntity.ok(Map.of(
                    "status", "running",
                    "registeredRepos", tokenStore.getRegisteredRepos().size(),
                    "version", "1.0.0"
            ));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                    "status", "running",
                    "registeredRepos", 0,
                    "version", "1.0.0",
                    "dbStatus", "unavailable"
            ));
        }
    }

    /**
     * Validate a GitHub token by calling GET /user.
     * Returns the username if valid, null if invalid.
     */
    private String validateGitHubToken(String token) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + token);
            headers.set("Accept", "application/vnd.github.v3+json");
            HttpEntity<Void> entity = new HttpEntity<>(headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    "https://api.github.com/user",
                    HttpMethod.GET, entity, Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return (String) response.getBody().get("login");
            }
        } catch (Exception e) {
            log.debug("Token validation failed: {}", e.getMessage());
        }
        return null;
    }
}





