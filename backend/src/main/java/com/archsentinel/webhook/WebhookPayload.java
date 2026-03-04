package com.archsentinel.webhook;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebhookPayload {

    private static final Logger log = LoggerFactory.getLogger(WebhookPayload.class);
    private static final ObjectMapper mapper = new ObjectMapper();

    private String eventType; // "pull_request" or "push"
    private String action;
    private int prNumber;
    private String repoFullName;
    private String baseBranch;
    private String headBranch;
    private String cloneUrl;
    private String beforeCommit;
    private String afterCommit;

    public WebhookPayload() {
    }

    public static WebhookPayload fromPullRequestJson(String json) {
        try {
            log.debug("Parsing pull_request webhook payload...");
            JsonNode root = mapper.readTree(json);
            WebhookPayload payload = new WebhookPayload();
            payload.eventType = "pull_request";

            payload.action = root.path("action").asText();
            log.debug("Action: {}", payload.action);

            JsonNode pr = root.path("pull_request");
            if (pr.isMissingNode()) {
                log.error("pull_request node is missing from payload");
                return null;
            }

            payload.prNumber = root.path("number").asInt();
            log.debug("PR Number: {}", payload.prNumber);

            payload.baseBranch = pr.path("base").path("ref").asText();
            log.debug("Base Branch: {}", payload.baseBranch);

            payload.headBranch = pr.path("head").path("ref").asText();
            log.debug("Head Branch: {}", payload.headBranch);

            payload.cloneUrl = pr.path("head").path("repo").path("clone_url").asText();
            log.debug("Clone URL: {}", payload.cloneUrl);

            payload.repoFullName = root.path("repository").path("full_name").asText();
            log.debug("Repo Full Name: {}", payload.repoFullName);

            if (payload.cloneUrl == null || payload.cloneUrl.isEmpty()) {
                log.error("Clone URL is missing or empty");
                return null;
            }

            log.info("✓ Pull request webhook payload parsed successfully");
            return payload;
        } catch (Exception e) {
            log.error("Failed to parse pull_request webhook payload: {}", e.getMessage(), e);
            return null;
        }
    }

    public static WebhookPayload fromPushJson(String json) {
        try {
            log.debug("Parsing push webhook payload...");
            JsonNode root = mapper.readTree(json);
            WebhookPayload payload = new WebhookPayload();
            payload.eventType = "push";
            payload.action = "push";

            // Extract branch name from ref (e.g., "refs/heads/main" -> "main")
            String ref = root.path("ref").asText();
            String branch = ref.replace("refs/heads/", "");
            log.debug("Branch from ref '{}': {}", ref, branch);

            payload.headBranch = branch;
            payload.baseBranch = branch; // For push, we'll compare before/after on same branch
            payload.beforeCommit = root.path("before").asText();
            payload.afterCommit = root.path("after").asText();
            log.debug("Before commit: {}, After commit: {}", payload.beforeCommit, payload.afterCommit);

            payload.cloneUrl = root.path("repository").path("clone_url").asText();
            log.debug("Clone URL: {}", payload.cloneUrl);

            payload.repoFullName = root.path("repository").path("full_name").asText();
            log.debug("Repo Full Name: {}", payload.repoFullName);

            payload.prNumber = 0; // No PR number for push events

            if (payload.cloneUrl == null || payload.cloneUrl.isEmpty()) {
                log.error("Clone URL is missing or empty");
                return null;
            }

            log.info("✓ Push webhook payload parsed successfully");
            return payload;
        } catch (Exception e) {
            log.error("Failed to parse push webhook payload: {}", e.getMessage(), e);
            return null;
        }
    }

    @Deprecated
    public static WebhookPayload fromJson(String json) {
        // Backward compatibility - defaults to pull_request parsing
        return fromPullRequestJson(json);
    }

    public String getAction() {
        return action;
    }

    public int getPrNumber() {
        return prNumber;
    }

    public String getRepoFullName() {
        return repoFullName;
    }

    public String getBaseBranch() {
        return baseBranch;
    }

    public String getHeadBranch() {
        return headBranch;
    }

    public String getCloneUrl() {
        return cloneUrl;
    }

    public String getEventType() {
        return eventType;
    }

    public String getBeforeCommit() {
        return beforeCommit;
    }

    public String getAfterCommit() {
        return afterCommit;
    }

    public boolean isPushEvent() {
        return "push".equals(eventType);
    }

    public boolean isPullRequestEvent() {
        return "pull_request".equals(eventType);
    }
}
