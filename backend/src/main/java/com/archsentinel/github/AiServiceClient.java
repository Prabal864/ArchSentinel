package com.archsentinel.github;

import com.archsentinel.diff.ImpactReport;
import com.archsentinel.graph.DependencyGraph;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class AiServiceClient {

    private static final Logger log = LoggerFactory.getLogger(AiServiceClient.class);

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    @Value("${archsentinel.ai-service.url:http://ai-service:8000}")
    private String aiServiceUrl;

    public AiServiceClient() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> analyze(ImpactReport report, DependencyGraph graph) {
        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("impactReport", buildReportMap(report));
            requestBody.put("graphData", graph.toMap());
            requestBody.put("violations", report.getPrSnapshot().getViolations());

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

            ResponseEntity<Map> response = restTemplate.exchange(
                    aiServiceUrl + "/analyze",
                    HttpMethod.POST,
                    entity,
                    Map.class);

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                return response.getBody();
            }
        } catch (Exception e) {
            log.warn("AI service unavailable: {}. Using fallback response.", e.getMessage());
        }
        return fallbackResponse(report);
    }

    private Map<String, Object> buildReportMap(ImpactReport report) {
        Map<String, Object> m = new HashMap<>();
        m.put("healthScoreChange", report.getHealthScoreChange());
        m.put("couplingDelta", report.getCouplingDelta());
        m.put("complexityDelta", report.getComplexityDelta());
        m.put("newCircularDependency", report.isNewCircularDependency());
        m.put("newCircularCount", report.getNewCircularCount());
        m.put("newLayerViolations", report.getNewLayerViolations());
        m.put("newPerformanceRisks", report.getNewPerformanceRisks());
        m.put("riskLevel", report.getRiskLevel());
        if (report.getPrSnapshot() != null) {
            var pr = report.getPrSnapshot();
            m.put("prHealthScore", pr.getHealthScore());
            m.put("circularPaths", pr.getCircularPaths());
            m.put("violations", pr.getViolations());
            m.put("risks", pr.getRisks());

            // === NEW: Pinpointed detail data ===
            m.put("totalClasses", pr.getTotalClasses());
            m.put("totalMethods", pr.getTotalMethods());

            // Top 10 coupling hotspots
            var hotspots = pr.getCouplingHotspots();
            m.put("couplingHotspots", hotspots.size() > 10 ? hotspots.subList(0, 10) : hotspots);

            // All complex methods (above threshold)
            m.put("complexMethods", pr.getComplexMethods());

            // Class details (top 15 by dependency count for brevity)
            var classDetails = pr.getClassDetails();
            classDetails.sort((a, b) -> Integer.compare(
                    (int) b.getOrDefault("dependencyCount", 0),
                    (int) a.getOrDefault("dependencyCount", 0)));
            m.put("classDetails", classDetails.size() > 15 ? classDetails.subList(0, 15) : classDetails);
        }
        if (report.getBaseSnapshot() != null) {
            m.put("baseHealthScore", report.getBaseSnapshot().getHealthScore());
            m.put("baseTotalClasses", report.getBaseSnapshot().getTotalClasses());
            m.put("baseTotalMethods", report.getBaseSnapshot().getTotalMethods());
        }
        return m;
    }

    private Map<String, Object> fallbackResponse(ImpactReport report) {
        Map<String, Object> fallback = new HashMap<>();
        fallback.put("explanation", buildFallbackExplanation(report));
        fallback.put("suggestions", buildFallbackSuggestions(report));
        fallback.put("riskAssessment", report.getRiskLevel());
        return fallback;
    }

    private String buildFallbackExplanation(ImpactReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("This PR introduces architectural changes with risk level: **").append(report.getRiskLevel()).append("**. ");
        if (report.getHealthScoreChange() < 0) {
            sb.append(String.format("Health score decreased by %.1f points. ", Math.abs(report.getHealthScoreChange())));
        }
        if (report.isNewCircularDependency()) {
            sb.append(String.format("%d new circular dependency(s) detected. ", report.getNewCircularCount()));
        }
        if (report.getNewLayerViolations() > 0) {
            sb.append(String.format("%d new layer violation(s) detected. ", report.getNewLayerViolations()));
        }
        return sb.toString();
    }

    private List<String> buildFallbackSuggestions(ImpactReport report) {
        java.util.List<String> suggestions = new java.util.ArrayList<>();
        if (report.isNewCircularDependency()) {
            suggestions.add("Resolve circular dependencies using dependency inversion or event-driven patterns.");
        }
        if (report.getNewLayerViolations() > 0) {
            suggestions.add("Ensure Controllers only depend on Services, not Repositories directly.");
        }
        if (report.getNewPerformanceRisks() > 0) {
            suggestions.add("Avoid N+1 queries by using batch loading or pagination.");
        }
        if (report.getCouplingDelta() > 10) {
            suggestions.add("Consider using facade pattern to reduce coupling.");
        }
        return suggestions;
    }
}
