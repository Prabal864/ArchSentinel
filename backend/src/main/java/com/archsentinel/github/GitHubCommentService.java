package com.archsentinel.github;

import com.archsentinel.diff.ImpactReport;
import com.archsentinel.metrics.ArchitectureSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class GitHubCommentService {

    private static final Logger log = LoggerFactory.getLogger(GitHubCommentService.class);

    private final GitHubApiClient apiClient;

    public GitHubCommentService(GitHubApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public boolean postComment(String repoFullName, int prNumber, ImpactReport report,
                            String aiExplanation, List<String> suggestions, String token) {
        String comment = buildComment(report, aiExplanation, suggestions);
        boolean success = apiClient.postComment(repoFullName, prNumber, comment, token);
        if (!success) {
            log.error("❌ Failed to post comment on PR #{} in '{}' — check token validity", prNumber, repoFullName);
        }
        return success;
    }

    public boolean postComment(String repoFullName, int prNumber, ImpactReport report,
                            String aiExplanation, List<String> suggestions) {
        return postComment(repoFullName, prNumber, report, aiExplanation, suggestions, null);
    }

    public boolean postCommentForBranch(String repoFullName, String branchName,
                                        ImpactReport report, String aiExplanation,
                                        List<String> suggestions, String token) {
        int prNumber = apiClient.findOpenPrNumber(repoFullName, branchName, token);
        if (prNumber > 0) {
            log.info("Found open PR #{} for branch '{}', posting comment...", prNumber, branchName);
            String comment = buildComment(report, aiExplanation, suggestions);
            boolean success = apiClient.postComment(repoFullName, prNumber, comment, token);
            if (success) {
                log.info("✓ Comment successfully posted on PR #{}", prNumber);
            } else {
                log.error("❌ Failed to post comment on PR #{} — check token validity", prNumber);
            }
            return success;
        }
        log.info("No open PR found for branch '{}', skipping comment", branchName);
        return false;
    }

    public boolean postCommentForBranch(String repoFullName, String branchName,
                                        ImpactReport report, String aiExplanation,
                                        List<String> suggestions) {
        return postCommentForBranch(repoFullName, branchName, report, aiExplanation, suggestions, null);
    }

    private String buildComment(ImpactReport report, String aiExplanation, List<String> suggestions) {
        ArchitectureSnapshot base = report.getBaseSnapshot();
        ArchitectureSnapshot pr = report.getPrSnapshot();
        StringBuilder sb = new StringBuilder();
        double baseScore = base.getHealthScore();
        double prScore = pr.getHealthScore();
        double delta = report.getHealthScoreChange();

        String grade = prScore >= 90 ? "A+" : prScore >= 80 ? "A" : prScore >= 70 ? "B" : prScore >= 60 ? "C" : prScore >= 50 ? "D" : "F";
        String gradeIcon = prScore >= 80 ? "\uD83D\uDFE2" : prScore >= 60 ? "\uD83D\uDFE1" : prScore >= 40 ? "\uD83D\uDFE0" : "\uD83D\uDD34";
        String trendStr = delta > 2 ? "\uD83D\uDCC8" : delta < -2 ? "\uD83D\uDCC9" : "\u27A1\uFE0F";
        String verdict = delta > 2 ? "Architecture improved!" : delta >= -2 ? "No major changes to architecture"
                : delta >= -10 ? "Minor architecture degradation" : "\u26A0\uFE0F Significant degradation detected";

        sb.append("## \uD83C\uDFD7\uFE0F ArchSentinel \u2014 Architecture Health Check\n\n");
        sb.append(String.format("> %s %s \u2014 **Grade %s** \u2014 Health: **%.0f / 100** (was %.0f) %s\n\n",
                gradeIcon, verdict, grade, prScore, baseScore, trendStr));
        int filled = (int) Math.round(prScore / 5);
        sb.append(String.format("> `%s` **%.0f%%**\n\n",
                "\u2588".repeat(Math.max(0, filled)) + "\u2591".repeat(Math.max(0, 20 - filled)), prScore));

        sb.append("### \uD83D\uDCCA Architecture Dashboard\n\n");
        sb.append("| Metric | Before | After | Trend |\n");
        sb.append("|:-------|:------:|:-----:|:-----:|\n");
        metricRow(sb, "\uD83C\uDFE5 Health Score",    f0(baseScore),                     f0(prScore),                  delta, false);
        metricRow(sb, "\uD83D\uDD17 Dependency Load", f1(base.getCouplingScore()),        f1(pr.getCouplingScore()),    report.getCouplingDelta(), true);
        metricRow(sb, "\uD83D\uDD04 Circular Refs",   si(base.getCircularDependencies()), si(pr.getCircularDependencies()), (double)(pr.getCircularDependencies() - base.getCircularDependencies()), true);
        metricRow(sb, "\uD83E\uDDE9 Avg Complexity",  f1(base.getComplexityScore()),      f1(pr.getComplexityScore()),  report.getComplexityDelta(), true);
        metricRow(sb, "\uD83D\uDEAB Layer Breaks",    si(base.getLayerViolations()),      si(pr.getLayerViolations()),  (double)(pr.getLayerViolations() - base.getLayerViolations()), true);
        metricRow(sb, "\uD83D\uDC0C Perf Risks",      si(base.getPerformanceRisks()),     si(pr.getPerformanceRisks()), (double)(pr.getPerformanceRisks() - base.getPerformanceRisks()), true);
        metricRow(sb, "\uD83D\uDCE6 Classes",         si(base.getTotalClasses()),         si(pr.getTotalClasses()),     (double)(pr.getTotalClasses() - base.getTotalClasses()), false);
        metricRow(sb, "\uD83D\uDCDD Methods",         si(base.getTotalMethods()),         si(pr.getTotalMethods()),     (double)(pr.getTotalMethods() - base.getTotalMethods()), false);
        sb.append("\n");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> nodes = (List<Map<String, String>>) pr.getGraphData().getOrDefault("nodes", List.of());
        int ctrls = 0, svcs = 0, reps = 0, ents = 0, utils = 0;
        for (var n : nodes) {
            switch (n.getOrDefault("type", "").toUpperCase()) {
                case "CONTROLLER" -> ctrls++;
                case "SERVICE"    -> svcs++;
                case "REPOSITORY" -> reps++;
                case "ENTITY"     -> ents++;
                default           -> utils++;
            }
        }
        int totalNodes = nodes.size();
        if (totalNodes > 0) {
            sb.append("<details>\n<summary>\uD83C\uDFDB\uFE0F Architecture Composition</summary>\n\n");
            sb.append("| Layer | Count | Share | Purpose |\n|:------|:-----:|:-----:|:--------|\n");
            if (ctrls > 0) sb.append(String.format("| \uD83C\uDFAE Controllers  | %d | %d%% | HTTP request handling |\n", ctrls, pct(ctrls, totalNodes)));
            if (svcs  > 0) sb.append(String.format("| \u2699\uFE0F Services      | %d | %d%% | Business logic |\n",       svcs,  pct(svcs,  totalNodes)));
            if (reps  > 0) sb.append(String.format("| \uD83D\uDCBE Repositories | %d | %d%% | Database access |\n",      reps,  pct(reps,  totalNodes)));
            if (ents  > 0) sb.append(String.format("| \uD83D\uDCE6 Models       | %d | %d%% | Data objects / DTOs |\n",  ents,  pct(ents,  totalNodes)));
            if (utils > 0) sb.append(String.format("| \uD83D\uDD27 Utilities    | %d | %d%% | Config, helpers, tests |\n", utils, pct(utils, totalNodes)));
            sb.append("\n");
            sb.append((ctrls > 0 && svcs > 0 && reps > 0)
                    ? "> \u2705 Clean layered architecture \u2014 Controller, Service, and Repository layers present\n"
                    : "> \u26A0\uFE0F Incomplete layering \u2014 consider adding missing layers\n");
            sb.append("\n</details>\n\n");
        }

        double stability = computeStability(pr);
        String stabLabel = stability >= 80 ? "Stable" : stability >= 60 ? "Moderate" : stability >= 40 ? "Fragile" : "Brittle";
        String stabIcon  = stability >= 80 ? "\uD83D\uDFE2" : stability >= 60 ? "\uD83D\uDFE1" : stability >= 40 ? "\uD83D\uDFE0" : "\uD83D\uDD34";
        sb.append("<details>\n<summary>\uD83D\uDD2E Stability & Maintainability Forecast</summary>\n\n");
        sb.append("| Indicator | Rating | What it means |\n|:----------|:------:|:--------------|\n");
        sb.append(String.format("| \uD83C\uDFD7\uFE0F Stability   | %s **%s** (%.0f%%) | Will changes break things? |\n", stabIcon, stabLabel, stability));
        sb.append(String.format("| \uD83D\uDCA5 Change Risk | %s | Chance one change triggers cascading bugs |\n",
                pr.getCircularDependencies() > 0 ? "\uD83D\uDD34 High" : pr.getCouplingScore() > 6 ? "\uD83D\uDFE0 Medium" : "\uD83D\uDFE2 Low"));
        sb.append(String.format("| \uD83E\uDDEA Testability | %s | How easy to write isolated unit tests |\n",
                pr.getCircularDependencies() > 0 ? "\uD83D\uDD34 Hard" : pr.getCouplingScore() > 5 ? "\uD83D\uDFE1 Needs mocking" : "\uD83D\uDFE2 Easy"));
        sb.append(String.format("| \uD83D\uDC64 Onboarding  | %s | How fast a new dev gets productive |\n",
                pr.getComplexityScore() > 15 ? "\uD83D\uDD34 Steep curve" : pr.getComplexityScore() > 8 ? "\uD83D\uDFE1 Some study" : "\uD83D\uDFE2 Quick"));
        sb.append(String.format("| \uD83D\uDCD0 Scalability | %s | Can this grow? (%d classes) |\n",
                pr.getTotalClasses() > 100 ? "\uD83D\uDFE1 Split modules" : "\uD83D\uDFE2 Room to grow", pr.getTotalClasses()));
        sb.append("\n**If current trends continue:**\n\n");
        boolean anyWarn = false;
        if (pr.getCircularDependencies() > 0) { sb.append("- \u26A0\uFE0F Circular deps will make every refactor harder\n"); anyWarn = true; }
        if (pr.getPerformanceRisks() > 3)     { sb.append("- \u26A0\uFE0F N+1 patterns cause slowdowns above ~1K records\n"); anyWarn = true; }
        if (pr.getCouplingScore() > 6)        { sb.append(String.format("- \u26A0\uFE0F High coupling: changes may ripple through %d+ classes\n", (int) pr.getCouplingScore())); anyWarn = true; }
        if (pr.getLayerViolations() > 0)      { sb.append("- \u26A0\uFE0F Layer violations make DB/framework changes expensive\n"); anyWarn = true; }
        if (!anyWarn) sb.append("- \u2705 Architecture is solid \u2014 no major risks on the horizon\n");
        sb.append("\n</details>\n\n");

        sb.append(buildMermaidDiagram(pr));

        int issueCount = pr.getViolations().size() + pr.getRisks().size() + pr.getCircularPaths().size();
        if (issueCount > 0) sb.append(String.format("### \uD83D\uDD0D Issues Found (%d)\n\n", issueCount));

        if (!pr.getViolations().isEmpty()) {
            sb.append(String.format("<details>\n<summary>\uD83D\uDEAB Layer Violations (%d)</summary>\n\n", pr.getViolations().size()));
            sb.append("> **Rule:** Controller \u2192 Service \u2192 Repository. Shortcuts create tight coupling.\n\n");
            sb.append("| # | What's happening | Why it's bad | How to fix |\n|:-:|:-----------------|:-------------|:-----------|\n");
            int i = 1;
            for (String v : pr.getViolations())
                sb.append(String.format("| %d | %s | DB changes require controller changes | Add a Service class between them |\n",
                        i++, v.replace("LAYER_VIOLATION: ", "").replace("|", "/")));
            sb.append("\n</details>\n\n");
        }
        if (!pr.getRisks().isEmpty()) {
            sb.append(String.format("<details>\n<summary>\uD83D\uDC0C Performance Risks (%d)</summary>\n\n", pr.getRisks().size()));
            sb.append("> **Problem:** DB calls inside loops = 1 query per item. 10K items = 10K queries.\n\n");
            sb.append("| # | Where | Issue | Impact at scale |\n|:-:|:------|:------|:----------------|\n");
            int i = 1;
            for (String r : pr.getRisks())
                sb.append(String.format("| %d | `%s` | DB call inside loop | ~10x slower per 10x data growth |\n",
                        i++, r.replace("PERFORMANCE_RISK: Potential N+1 query in ", "").replace("PERFORMANCE_RISK: ", "")));
            sb.append("\n</details>\n\n");
        }
        if (!pr.getCircularPaths().isEmpty()) {
            sb.append(String.format("<details>\n<summary>\uD83D\uDD04 Circular Dependencies (%d)</summary>\n\n", pr.getCircularPaths().size()));
            sb.append("> **Problem:** A \u2192 B \u2192 A means you can't deploy or test them independently.\n\n");
            for (String c : pr.getCircularPaths())
                sb.append("- \uD83D\uDD04 `").append(c).append("` \u2014 extract an interface to break the cycle\n");
            sb.append("\n</details>\n\n");
        }
        if (!pr.getCouplingHotspots().isEmpty()) {
            sb.append("<details>\n<summary>\uD83D\uDD17 Dependency Hotspots \u2014 most connected classes</summary>\n\n");
            sb.append("> Target: < 5 total connections per class.\n\n");
            sb.append("| Class | Role | Depends on | Used by | Total | Risk |\n|:------|:-----|:----------:|:-------:|:-----:|:----:|\n");
            int ct = 0;
            for (Map<String, Object> h : pr.getCouplingHotspots()) {
                if (ct++ >= 10) break;
                int tc = (int) h.getOrDefault("totalCoupling", 0);
                sb.append(String.format("| `%s` | %s | %s | %s | **%d** | %s |\n",
                        h.get("className"), typeLabel((String) h.getOrDefault("type", "")),
                        h.get("fanOut"), h.get("fanIn"), tc,
                        tc > 7 ? "\uD83D\uDD34 Critical" : tc > 4 ? "\uD83D\uDFE1 Watch" : "\uD83D\uDFE2 Good"));
            }
            sb.append("\n</details>\n\n");
        }
        if (!pr.getComplexMethods().isEmpty()) {
            sb.append("<details>\n<summary>\uD83E\uDDE9 Complex Methods \u2014 hardest to maintain</summary>\n\n");
            sb.append("> More branches = harder to test, read, debug.\n\n");
            sb.append("| Class | Method | Branches | Difficulty | Suggested action |\n|:------|:-------|:--------:|:----------:|:-----------------|\n");
            for (Map<String, Object> cm : pr.getComplexMethods()) {
                int c = ((Number) cm.getOrDefault("complexity", 0)).intValue();
                sb.append(String.format("| `%s` | `%s()` | %d | %s | %s |\n",
                        cm.get("className"), cm.get("methodName"), c,
                        c >= 20 ? "\uD83D\uDD34 Very hard" : c >= 10 ? "\uD83D\uDFE0 Hard" : c >= 7 ? "\uD83D\uDFE1 Moderate" : "\uD83D\uDFE2 Fine",
                        c >= 10 ? "Split into smaller methods" : "OK for now"));
            }
            sb.append("\n</details>\n\n");
        }
        if (aiExplanation != null && !aiExplanation.isEmpty()) {
            sb.append("### \uD83E\uDD16 AI Architecture Review\n\n");
            sb.append(aiExplanation).append("\n\n");
        }
        sb.append("---\n*\uD83C\uDFD7\uFE0F [ArchSentinel AI](https://github.com/Prabal0202/ArchitectureDoctor) \u2014 Architecture health on every PR*\n");
        return sb.toString();
    }

    private String buildMermaidDiagram(ArchitectureSnapshot snapshot) {
        StringBuilder sb = new StringBuilder();
        sb.append("<details open>\n<summary>\uD83D\uDCCA How Your Code Is Connected (Architecture Diagram)</summary>\n\n");
        sb.append("```mermaid\ngraph TD\n");
        sb.append("    classDef controller fill:#e74c3c,stroke:#c0392b,color:#fff,stroke-width:2px\n");
        sb.append("    classDef service    fill:#2ecc71,stroke:#27ae60,color:#fff,stroke-width:2px\n");
        sb.append("    classDef repository fill:#3498db,stroke:#2980b9,color:#fff,stroke-width:2px\n");
        sb.append("    classDef entity     fill:#f39c12,stroke:#e67e22,color:#fff,stroke-width:2px\n");
        sb.append("    classDef unknown    fill:#95a5a6,stroke:#7f8c8d,color:#fff,stroke-width:1px\n\n");

        @SuppressWarnings("unchecked")
        List<Map<String, String>> nodes = (List<Map<String, String>>) snapshot.getGraphData().getOrDefault("nodes", List.of());
        @SuppressWarnings("unchecked")
        List<Map<String, String>> edges = (List<Map<String, String>>) snapshot.getGraphData().getOrDefault("edges", List.of());

        StringBuilder ctrlSb = new StringBuilder(), svcSb = new StringBuilder(),
                repoSb = new StringBuilder(), entSb = new StringBuilder(), otherSb = new StringBuilder();

        for (Map<String, String> node : nodes) {
            String name   = node.getOrDefault("name", "Unknown");
            String type   = node.getOrDefault("type", "UNKNOWN").toUpperCase();
            String safeId = name.replaceAll("[^a-zA-Z0-9]", "_");
            switch (type) {
                case "CONTROLLER" -> ctrlSb.append("        ").append(safeId).append("[\"").append(name).append("\"]\n");
                case "SERVICE"    -> svcSb .append("        ").append(safeId).append("[\"").append(name).append("\"]\n");
                case "REPOSITORY" -> repoSb.append("        ").append(safeId).append("[(\"").append(name).append("\")]\n");
                case "ENTITY"     -> entSb .append("        ").append(safeId).append("((\"").append(name).append("\"))\n");
                default           -> otherSb.append("        ").append(safeId).append("[\"").append(name).append("\"]\n");
            }
        }

        if (!ctrlSb.isEmpty())  sb.append("    subgraph API[\"API Layer - Handles Requests\"]\n        direction LR\n").append(ctrlSb).append("    end\n");
        if (!svcSb.isEmpty())   sb.append("    subgraph BIZ[\"Business Layer - Core Logic\"]\n        direction LR\n").append(svcSb).append("    end\n");
        if (!repoSb.isEmpty())  sb.append("    subgraph DATA[\"Data Layer - Database Access\"]\n        direction LR\n").append(repoSb).append("    end\n");
        if (!entSb.isEmpty())   sb.append("    subgraph MODELS[\"Models - Data Objects\"]\n        direction LR\n").append(entSb).append("    end\n");
        if (!otherSb.isEmpty()) sb.append("    subgraph UTIL[\"Utilities and Config\"]\n        direction LR\n").append(otherSb).append("    end\n");
        sb.append("\n");

        for (Map<String, String> edge : edges) {
            String from = edge.getOrDefault("from", "").replaceAll("[^a-zA-Z0-9]", "_");
            String to   = edge.getOrDefault("to",   "").replaceAll("[^a-zA-Z0-9]", "_");
            if (!from.isEmpty() && !to.isEmpty()) {
                boolean violation = snapshot.getViolations().stream()
                        .anyMatch(v -> v.contains(edge.getOrDefault("from", "")) && v.contains(edge.getOrDefault("to", "")));
                sb.append(violation
                        ? "    " + from + " -.->|\"SKIPS LAYER\"| " + to + "\n"
                        : "    " + from + " --> " + to + "\n");
            }
        }

        for (Map<String, String> node : nodes) {
            String name = node.getOrDefault("name", "").replaceAll("[^a-zA-Z0-9]", "_");
            String type = node.getOrDefault("type", "UNKNOWN").toLowerCase();
            if (!name.isEmpty()) sb.append("    class ").append(name).append(" ").append(type).append("\n");
        }
        sb.append("```\n\n");
        sb.append("> **Legend:** Controller (red) → Service (green) → Repository (blue) → Model (orange) | Dashed arrow = skips a layer (bad)\n\n");
        sb.append("</details>\n\n");
        return sb.toString();
    }

    private double computeStability(ArchitectureSnapshot s) {
        double score = 100;
        score -= s.getCircularDependencies() * 15;
        score -= s.getLayerViolations() * 10;
        score -= s.getPerformanceRisks() * 3;
        score -= Math.max(0, s.getCouplingScore() - 3) * 5;
        score -= Math.max(0, s.getComplexityScore() - 5) * 2;
        return Math.max(0, Math.min(100, score));
    }

    private void metricRow(StringBuilder sb, String name, String before, String after, double delta, boolean lowerBetter) {
        String trend;
        if (Math.abs(delta) < 0.01)  trend = "\u27A1\uFE0F Same";
        else if (lowerBetter)        trend = delta > 0 ? String.format("\uD83D\uDD34 +%.1f", delta) : String.format("\uD83D\uDFE2 %.1f", delta);
        else                         trend = delta > 0 ? String.format("\uD83D\uDFE2 +%.1f", delta) : String.format("\uD83D\uDD34 %.1f", delta);
        sb.append(String.format("| %s | %s | %s | %s |\n", name, before, after, trend));
    }

    private String typeLabel(String type) {
        return switch (type.toUpperCase()) {
            case "CONTROLLER" -> "\uD83C\uDFAE Controller";
            case "SERVICE"    -> "\u2699\uFE0F Service";
            case "REPOSITORY" -> "\uD83D\uDCBE Repository";
            case "ENTITY"     -> "\uD83D\uDCE6 Model";
            default           -> "\uD83D\uDD27 Utility";
        };
    }

    private String f0(double v) { return String.format("%.0f", v); }
    private String f1(double v) { return String.format("%.1f", v); }
    private String si(int v)    { return String.valueOf(v); }
    private int pct(int part, int total) { return total == 0 ? 0 : Math.round(part * 100f / total); }
}




