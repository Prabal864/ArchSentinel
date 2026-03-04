package com.archsentinel.repo;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@ConfigurationProperties(prefix = "archsentinel")
public class RepoClonerConfig {

    private String cloneDir = "/tmp/archsentinel-repos";
    private Github github = new Github();

    public Path getCloneDir() {
        Path dir = Paths.get(cloneDir);
        if (!Files.exists(dir)) {
            try {
                Files.createDirectories(dir);
            } catch (Exception e) {
                // fallback to /tmp
                return Paths.get("/tmp");
            }
        }
        return dir;
    }

    public void setCloneDir(String cloneDir) {
        this.cloneDir = cloneDir;
    }

    public String getGithubToken() {
        return github.getToken();
    }

    public Github getGithub() {
        return github;
    }

    public void setGithub(Github github) {
        this.github = github;
    }

    public static class Github {
        private String token = "";
        private String webhookSecret = "";

        public String getToken() {
            return token;
        }

        public void setToken(String token) {
            this.token = token;
        }

        public String getWebhookSecret() {
            return webhookSecret;
        }

        public void setWebhookSecret(String webhookSecret) {
            this.webhookSecret = webhookSecret;
        }
    }
}
