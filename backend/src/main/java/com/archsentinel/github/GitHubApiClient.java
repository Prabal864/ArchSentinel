package com.archsentinel.github;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Component
public class GitHubApiClient {

    private static final Logger log = LoggerFactory.getLogger(GitHubApiClient.class);
    private static final String GITHUB_API_BASE = "https://api.github.com";

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${archsentinel.github.token:}")
    private String fallbackToken;

    public GitHubApiClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    // ======================== Post Comment ========================

    public boolean postComment(String repoFullName, int prNumber, String body, String token) {
        String resolvedToken = resolveToken(token);
        if (resolvedToken == null || resolvedToken.isEmpty()) {
            log.error("❌ Cannot post comment on PR #{}: no GitHub token available for repo '{}'", prNumber, repoFullName);
            return false;
        }

        String url = String.format("%s/repos/%s/issues/%d/comments", GITHUB_API_BASE, repoFullName, prNumber);
        HttpHeaders headers = buildHeaders(resolvedToken);
        Map<String, String> requestBody = Map.of("body", body);
        HttpEntity<Map<String, String>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✓ Comment posted on PR #{} in {}", prNumber, repoFullName);
                return true;
            } else {
                log.error("❌ Failed to post comment on PR #{}: HTTP {}", prNumber, response.getStatusCode());
                return false;
            }
        } catch (Exception e) {
            log.error("❌ Error posting comment to GitHub PR #{}: {}", prNumber, e.getMessage());
            return false;
        }
    }

    /** Fallback: uses env token */
    public boolean postComment(String repoFullName, int prNumber, String body) {
        return postComment(repoFullName, prNumber, body, null);
    }

    // ======================== Find Open PR ========================

    public int findOpenPrNumber(String repoFullName, String branchName, String token) {
        String url = String.format("%s/repos/%s/pulls?state=open&head=%s:%s",
                GITHUB_API_BASE, repoFullName,
                repoFullName.split("/")[0], branchName);

        HttpHeaders headers = buildHeaders(resolveToken(token));
        HttpEntity<Void> entity = new HttpEntity<>(headers);

        try {
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.GET, entity, String.class);
            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                JsonNode pulls = objectMapper.readTree(response.getBody());
                if (pulls.isArray() && !pulls.isEmpty()) {
                    int prNumber = pulls.get(0).path("number").asInt();
                    log.info("✓ Found open PR #{} for branch '{}'", prNumber, branchName);
                    return prNumber;
                }
            }
            log.info("No open PR found for branch '{}'", branchName);
        } catch (Exception e) {
            log.warn("Could not search for open PRs: {}", e.getMessage());
        }
        return -1;
    }

    /** Fallback: uses env token */
    public int findOpenPrNumber(String repoFullName, String branchName) {
        return findOpenPrNumber(repoFullName, branchName, null);
    }

    // ======================== Helpers ========================

    private String resolveToken(String token) {
        if (token != null && !token.isEmpty()) return token;
        return fallbackToken;
    }

    private HttpHeaders buildHeaders(String token) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Accept", "application/vnd.github.v3+json");
        if (token != null && !token.isEmpty()) {
            headers.set("Authorization", "token " + token);
        }
        return headers;
    }
}
