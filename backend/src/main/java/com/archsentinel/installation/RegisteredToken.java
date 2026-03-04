package com.archsentinel.installation;

import jakarta.persistence.*;
import java.time.Instant;

/**
 * Persists user tokens in PostgreSQL so they survive server restarts.
 * Each row = one repo → token mapping.
 */
@Entity
@Table(name = "registered_tokens", indexes = {
        @Index(name = "idx_repo", columnList = "repoFullName", unique = true),
        @Index(name = "idx_username", columnList = "githubUsername")
})
public class RegisteredToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** e.g. "prabal0202/architecturedoctor" (always lowercase) */
    @Column(nullable = false, unique = true)
    private String repoFullName;

    /** The GitHub PAT — encrypted in production, plain for MVP */
    @Column(nullable = false, length = 512)
    private String token;

    /** GitHub username who registered this token */
    @Column(length = 100)
    private String githubUsername;

    /** "PAT" or "GITHUB_APP" */
    @Column(nullable = false, length = 20)
    private String tokenType = "PAT";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private Instant updatedAt = Instant.now();

    public RegisteredToken() {}

    public RegisteredToken(String repoFullName, String token, String githubUsername) {
        this.repoFullName = repoFullName.toLowerCase();
        this.token = token;
        this.githubUsername = githubUsername;
        this.tokenType = "PAT";
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    // ========== Getters & Setters ==========

    public Long getId() { return id; }

    public String getRepoFullName() { return repoFullName; }
    public void setRepoFullName(String repoFullName) { this.repoFullName = repoFullName.toLowerCase(); }

    public String getToken() { return token; }
    public void setToken(String token) {
        this.token = token;
        this.updatedAt = Instant.now();
    }

    public String getGithubUsername() { return githubUsername; }
    public void setGithubUsername(String githubUsername) { this.githubUsername = githubUsername; }

    public String getTokenType() { return tokenType; }
    public void setTokenType(String tokenType) { this.tokenType = tokenType; }

    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }

    /** Mask the token for display: "ghp_****abcd" */
    public String getMaskedToken() {
        if (token == null || token.length() < 8) return "****";
        return token.substring(0, 4) + "****" + token.substring(token.length() - 4);
    }
}

