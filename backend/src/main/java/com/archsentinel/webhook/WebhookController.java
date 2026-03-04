package com.archsentinel.webhook;

import com.archsentinel.analyzer.StaticAnalyzer;
import com.archsentinel.diff.DiffEngine;
import com.archsentinel.diff.ImpactReport;
import com.archsentinel.github.AiServiceClient;
import com.archsentinel.github.GitHubCommentService;
import com.archsentinel.graph.GraphBuilder;
import com.archsentinel.installation.InstallationTokenStore;
import com.archsentinel.metrics.ArchitectureSnapshot;
import com.archsentinel.metrics.MetricsEngine;
import com.archsentinel.repo.RepoCloner;
import com.archsentinel.scoring.HealthScoreCalculator;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/webhook")
public class WebhookController {

    private static final Logger log = LoggerFactory.getLogger(WebhookController.class);

    private final WebhookSignatureValidator signatureValidator;
    private final RepoCloner repoCloner;
    private final StaticAnalyzer staticAnalyzer;
    private final GraphBuilder graphBuilder;
    private final MetricsEngine metricsEngine;
    private final DiffEngine diffEngine;
    private final HealthScoreCalculator healthScoreCalculator;
    private final GitHubCommentService commentService;
    private final AiServiceClient aiServiceClient;
    private final InstallationTokenStore tokenStore;

    public WebhookController(WebhookSignatureValidator signatureValidator,
                             RepoCloner repoCloner,
                             StaticAnalyzer staticAnalyzer,
                             GraphBuilder graphBuilder,
                             MetricsEngine metricsEngine,
                             DiffEngine diffEngine,
                             HealthScoreCalculator healthScoreCalculator,
                             GitHubCommentService commentService,
                             AiServiceClient aiServiceClient,
                             InstallationTokenStore tokenStore) {
        this.signatureValidator = signatureValidator;
        this.repoCloner = repoCloner;
        this.staticAnalyzer = staticAnalyzer;
        this.graphBuilder = graphBuilder;
        this.metricsEngine = metricsEngine;
        this.diffEngine = diffEngine;
        this.healthScoreCalculator = healthScoreCalculator;
        this.commentService = commentService;
        this.aiServiceClient = aiServiceClient;
        this.tokenStore = tokenStore;
    }

    @PostMapping("/github")
    public ResponseEntity<String> handleWebhook(
            @RequestHeader(value = "X-Hub-Signature-256", required = false) String signature,
            @RequestHeader(value = "X-GitHub-Event", defaultValue = "ping") String event,
            @RequestBody String payload,
            HttpServletRequest request) {

        log.info("=== Webhook received: Event={}, Signature={} ===", event, signature != null ? "present" : "missing");
        log.debug("Payload preview: {}", payload.length() > 200 ? payload.substring(0, 200) + "..." : payload);

        if (!signatureValidator.isValid(payload, signature)) {
            log.warn("❌ Invalid webhook signature received");
            return ResponseEntity.status(401).body("Invalid signature");
        }
        log.info("✓ Signature validated successfully");

        if ("ping".equals(event)) {
            log.info("Ping event received - responding with pong");
            return ResponseEntity.ok("pong");
        }

        // Handle pull_request events
        if ("pull_request".equals(event)) {
            log.info("Processing pull_request event...");
            WebhookPayload webhookPayload = WebhookPayload.fromPullRequestJson(payload);
            if (webhookPayload == null) {
                log.error("❌ Failed to parse pull_request webhook payload");
                return ResponseEntity.badRequest().body("Invalid pull_request payload");
            }
            log.info("✓ Payload parsed successfully");

            String action = webhookPayload.getAction();
            log.info("PR action: {}", action);
            if (!"opened".equals(action) && !"synchronize".equals(action)) {
                log.info("PR action '{}' is ignored (only 'opened' and 'synchronize' are processed)", action);
                return ResponseEntity.ok("PR action ignored: " + action);
            }

            log.info("✓ Processing PR #{} for repo {}", webhookPayload.getPrNumber(), webhookPayload.getRepoFullName());
            log.info("Base branch: {}, Head branch: {}", webhookPayload.getBaseBranch(), webhookPayload.getHeadBranch());

            try {
                triggerAnalysis(webhookPayload);
                log.info("✓ Analysis triggered successfully for PR #{}", webhookPayload.getPrNumber());
            } catch (Exception e) {
                log.error("❌ Failed to trigger analysis: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body("Failed to trigger analysis: " + e.getMessage());
            }

            return ResponseEntity.ok("Analysis triggered for PR #" + webhookPayload.getPrNumber());
        }

        // Handle push events
        if ("push".equals(event)) {
            log.info("Processing push event...");
            WebhookPayload webhookPayload = WebhookPayload.fromPushJson(payload);
            if (webhookPayload == null) {
                log.error("❌ Failed to parse push webhook payload");
                return ResponseEntity.badRequest().body("Invalid push payload");
            }
            log.info("✓ Payload parsed successfully");

            log.info("✓ Processing push to branch '{}' for repo {}",
                     webhookPayload.getHeadBranch(),
                     webhookPayload.getRepoFullName());
            log.info("Commits: {} -> {}",
                     webhookPayload.getBeforeCommit().substring(0, 7),
                     webhookPayload.getAfterCommit().substring(0, 7));

            try {
                triggerPushAnalysis(webhookPayload);
                log.info("✓ Push analysis triggered successfully for branch '{}'", webhookPayload.getHeadBranch());
            } catch (Exception e) {
                log.error("❌ Failed to trigger push analysis: {}", e.getMessage(), e);
                return ResponseEntity.status(500).body("Failed to trigger analysis: " + e.getMessage());
            }

            return ResponseEntity.ok("Analysis triggered for push to " + webhookPayload.getHeadBranch());
        }

        // Unsupported event type
        log.warn("Event '{}' is not supported, only 'pull_request' and 'push' events are processed", event);
        return ResponseEntity.ok("Event ignored: " + event);
    }

    @GetMapping("/test")
    public ResponseEntity<String> testEndpoint() {
        log.info("Test endpoint called - system is running");
        return ResponseEntity.ok("ArchSentinel backend is running!\n" +
                "Webhook endpoint: POST /webhook/github\n" +
                "Supported events: pull_request (opened, synchronize), push");
    }

    @Async
    protected void triggerPushAnalysis(WebhookPayload payload) {
        Path beforeDir = null;
        Path afterDir = null;
        try {
            log.info("⏳ Starting push analysis for branch '{}' in {}", 
                     payload.getHeadBranch(), payload.getRepoFullName());

            // Resolve the token for this repo (user token → app token → env fallback)
            String token = tokenStore.getTokenForRepo(payload.getRepoFullName());
            if (token == null) {
                log.warn("⚠️ No token found for repo {}. Clone may fail for private repos.", payload.getRepoFullName());
            }

            String beforeCommit = payload.getBeforeCommit();
            String afterCommit = payload.getAfterCommit();
            boolean isFirstPush = beforeCommit != null && beforeCommit.matches("^0+$");

            if (isFirstPush) {
                log.info("First push to branch detected, analyzing current state only");

                log.info("Step 1/6: Cloning branch '{}'...", payload.getHeadBranch());
                afterDir = repoCloner.cloneBranch(payload.getCloneUrl(), payload.getHeadBranch(), token);
                log.info("✓ Branch cloned to: {}", afterDir);

                log.info("Step 2/6: Analyzing classes...");
                var afterClasses = staticAnalyzer.analyze(afterDir);
                log.info("✓ Found {} classes", afterClasses.size());

                log.info("Step 3/6: Building dependency graph...");
                var afterGraph = graphBuilder.build(afterClasses);
                log.info("✓ Dependency graph built ({} nodes)", afterGraph.getNodes().size());

                log.info("Step 4/6: Computing metrics...");
                ArchitectureSnapshot afterSnapshot = metricsEngine.compute(afterClasses, afterGraph);
                log.info("✓ Metrics computed");

                log.info("Step 5/6: Calculating health score...");
                afterSnapshot.setHealthScore(healthScoreCalculator.calculate(afterSnapshot));
                log.info("✓ Health score: {}", afterSnapshot.getHealthScore());

                // Compare with itself (no delta for first push)
                ImpactReport report = diffEngine.compare(afterSnapshot, afterSnapshot);

                log.info("Step 6/6: Getting AI analysis...");
                Map<String, Object> aiResponse = aiServiceClient.analyze(report, afterGraph);
                String explanation = (String) aiResponse.getOrDefault("explanation", "");
                @SuppressWarnings("unchecked")
                List<String> suggestions = (List<String>) aiResponse.getOrDefault("suggestions", List.of());
                log.info("✓ AI analysis complete");

                // Auto-post comment on the open PR for this branch
                log.info("Searching for open PR for branch '{}'...", payload.getHeadBranch());
                boolean posted = commentService.postCommentForBranch(
                        payload.getRepoFullName(),
                        payload.getHeadBranch(),
                        report, explanation, suggestions, token);
                if (posted) {
                    log.info("✓ Comment posted on PR for branch '{}'", payload.getHeadBranch());
                } else {
                    log.info("No open PR found for branch '{}', report available in logs only", payload.getHeadBranch());
                }

            } else {
                // Normal push - compare before and after commits
                log.info("Step 1/8: Cloning repo for before commit '{}'...", beforeCommit.substring(0, 7));
                beforeDir = repoCloner.cloneAtCommit(payload.getCloneUrl(), beforeCommit, token);
                log.info("✓ Before commit cloned to: {}", beforeDir);

                log.info("Step 2/8: Cloning repo for after commit '{}'...", afterCommit.substring(0, 7));
                afterDir = repoCloner.cloneAtCommit(payload.getCloneUrl(), afterCommit, token);
                log.info("✓ After commit cloned to: {}", afterDir);

                log.info("Step 3/8: Analyzing before commit classes...");
                var beforeClasses = staticAnalyzer.analyze(beforeDir);
                log.info("✓ Found {} classes in before commit", beforeClasses.size());

                log.info("Step 4/8: Analyzing after commit classes...");
                var afterClasses = staticAnalyzer.analyze(afterDir);
                log.info("✓ Found {} classes in after commit", afterClasses.size());

                log.info("Step 5/8: Building dependency graphs...");
                var beforeGraph = graphBuilder.build(beforeClasses);
                var afterGraph = graphBuilder.build(afterClasses);
                log.info("✓ Dependency graphs built (before: {} nodes, after: {} nodes)",
                         beforeGraph.getNodes().size(), afterGraph.getNodes().size());

                log.info("Step 6/8: Computing metrics...");
                ArchitectureSnapshot beforeSnapshot = metricsEngine.compute(beforeClasses, beforeGraph);
                ArchitectureSnapshot afterSnapshot = metricsEngine.compute(afterClasses, afterGraph);
                log.info("✓ Metrics computed");

                log.info("Step 7/8: Calculating health scores...");
                beforeSnapshot.setHealthScore(healthScoreCalculator.calculate(beforeSnapshot));
                afterSnapshot.setHealthScore(healthScoreCalculator.calculate(afterSnapshot));
                log.info("✓ Health scores calculated (before: {}, after: {})",
                         beforeSnapshot.getHealthScore(), afterSnapshot.getHealthScore());

                ImpactReport report = diffEngine.compare(beforeSnapshot, afterSnapshot);
                log.info("✓ Impact report generated");

                log.info("Step 8/8: Getting AI analysis...");
                Map<String, Object> aiResponse = aiServiceClient.analyze(report, afterGraph);
                String explanation = (String) aiResponse.getOrDefault("explanation", "");
                @SuppressWarnings("unchecked")
                List<String> suggestions = (List<String>) aiResponse.getOrDefault("suggestions", List.of());
                log.info("✓ AI analysis complete");

                // Auto-post comment on the open PR for this branch
                log.info("Searching for open PR for branch '{}'...", payload.getHeadBranch());
                boolean posted = commentService.postCommentForBranch(
                        payload.getRepoFullName(),
                        payload.getHeadBranch(),
                        report, explanation, suggestions, token);
                if (posted) {
                    log.info("✓ Comment automatically posted on PR for branch '{}'", payload.getHeadBranch());
                } else {
                    log.info("No open PR found for branch '{}', report available in logs only", payload.getHeadBranch());
                }

                log.info("📊 Push Analysis Summary:");
                log.info("   Branch: {}", payload.getHeadBranch());
                log.info("   Commits: {} -> {}", beforeCommit.substring(0, 7), afterCommit.substring(0, 7));
                log.info("   Health Score Change: {} -> {} (Δ {})",
                         beforeSnapshot.getHealthScore(),
                         afterSnapshot.getHealthScore(),
                         afterSnapshot.getHealthScore() - beforeSnapshot.getHealthScore());
            }

            log.info("✅ Push analysis complete for branch '{}'", payload.getHeadBranch());
        } catch (Exception e) {
            log.error("❌ Push analysis failed for branch '{}': {}", payload.getHeadBranch(), e.getMessage(), e);
        } finally {
            if (beforeDir != null) {
                log.debug("Cleaning up before directory: {}", beforeDir);
                repoCloner.cleanup(beforeDir);
            }
            if (afterDir != null) {
                log.debug("Cleaning up after directory: {}", afterDir);
                repoCloner.cleanup(afterDir);
            }
        }
    }

    @Async
    protected void triggerAnalysis(WebhookPayload payload) {
        Path baseDir = null;
        Path prDir = null;
        try {
            log.info("⏳ Starting analysis for PR #{} in {}", payload.getPrNumber(), payload.getRepoFullName());

            // Resolve the token for this repo
            String token = tokenStore.getTokenForRepo(payload.getRepoFullName());
            if (token == null) {
                log.warn("⚠️ No token found for repo {}. Clone may fail for private repos.", payload.getRepoFullName());
            }

            log.info("Step 1/8: Cloning base branch '{}'...", payload.getBaseBranch());
            baseDir = repoCloner.cloneBranch(payload.getCloneUrl(), payload.getBaseBranch(), token);
            log.info("✓ Base branch cloned to: {}", baseDir);

            log.info("Step 2/8: Cloning head branch '{}'...", payload.getHeadBranch());
            prDir = repoCloner.cloneBranch(payload.getCloneUrl(), payload.getHeadBranch(), token);
            log.info("✓ Head branch cloned to: {}", prDir);

            log.info("Step 3/8: Analyzing base branch classes...");
            var baseClasses = staticAnalyzer.analyze(baseDir);
            log.info("✓ Found {} classes in base branch", baseClasses.size());

            log.info("Step 4/8: Analyzing PR branch classes...");
            var prClasses = staticAnalyzer.analyze(prDir);
            log.info("✓ Found {} classes in PR branch", prClasses.size());

            log.info("Step 5/8: Building dependency graphs...");
            var baseGraph = graphBuilder.build(baseClasses);
            var prGraph = graphBuilder.build(prClasses);
            log.info("✓ Dependency graphs built (base: {} nodes, pr: {} nodes)",
                     baseGraph.getNodes().size(), prGraph.getNodes().size());

            log.info("Step 6/8: Computing metrics...");
            ArchitectureSnapshot baseSnapshot = metricsEngine.compute(baseClasses, baseGraph);
            ArchitectureSnapshot prSnapshot = metricsEngine.compute(prClasses, prGraph);
            log.info("✓ Metrics computed");

            log.info("Step 7/8: Calculating health scores...");
            baseSnapshot.setHealthScore(healthScoreCalculator.calculate(baseSnapshot));
            prSnapshot.setHealthScore(healthScoreCalculator.calculate(prSnapshot));
            log.info("✓ Health scores calculated (base: {}, pr: {})",
                     baseSnapshot.getHealthScore(), prSnapshot.getHealthScore());

            ImpactReport report = diffEngine.compare(baseSnapshot, prSnapshot);
            log.info("✓ Impact report generated");

            log.info("Step 8/8: Getting AI analysis...");
            Map<String, Object> aiResponse = aiServiceClient.analyze(report, prGraph);
            String explanation = (String) aiResponse.getOrDefault("explanation", "");
            @SuppressWarnings("unchecked")
            List<String> suggestions = (List<String>) aiResponse.getOrDefault("suggestions", List.of());
            log.info("✓ AI analysis complete");

            log.info("Posting comment to GitHub PR #{}...", payload.getPrNumber());
            boolean posted = commentService.postComment(
                    payload.getRepoFullName(),
                    payload.getPrNumber(),
                    report,
                    explanation,
                    suggestions,
                    token);
            if (posted) {
                log.info("✓ Comment posted on PR #{}", payload.getPrNumber());
            } else {
                log.error("❌ Failed to post comment on PR #{} — check token for repo '{}'",
                        payload.getPrNumber(), payload.getRepoFullName());
            }

            log.info("✅ Analysis complete for PR #{}", payload.getPrNumber());
        } catch (Exception e) {
            log.error("❌ Analysis failed for PR #{}: {}", payload.getPrNumber(), e.getMessage(), e);
        } finally {
            if (baseDir != null) {
                log.debug("Cleaning up base directory: {}", baseDir);
                repoCloner.cleanup(baseDir);
            }
            if (prDir != null) {
                log.debug("Cleaning up PR directory: {}", prDir);
                repoCloner.cleanup(prDir);
            }
        }
    }
}
