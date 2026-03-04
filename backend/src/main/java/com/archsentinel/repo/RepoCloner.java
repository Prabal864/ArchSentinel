package com.archsentinel.repo;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ResetCommand;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.stream.Stream;

@Service
public class RepoCloner {

    private static final Logger log = LoggerFactory.getLogger(RepoCloner.class);
    private static final String HEX_PATTERN = "^[0-9a-fA-F]{40}$";

    private final RepoClonerConfig config;

    public RepoCloner(RepoClonerConfig config) {
        this.config = config;
    }

    /**
     * Clone a repo by branch name (shallow clone, depth=1).
     */
    public Path cloneBranch(String cloneUrl, String branch, String token) throws Exception {
        if (isCommitSha(branch)) {
            return cloneAtCommit(cloneUrl, branch, token);
        }

        Path targetDir = Files.createTempDirectory(config.getCloneDir(), "repo-");
        log.info("Cloning {} branch {} to {}", cloneUrl, branch, targetDir);

        String effectiveToken = resolveToken(token);
        Git.cloneRepository()
                .setURI(addTokenToUrl(cloneUrl, effectiveToken))
                .setDirectory(targetDir.toFile())
                .setBranch(branch)
                .setDepth(1)
                .setCredentialsProvider(getCredentialsProvider(effectiveToken))
                .call()
                .close();

        log.info("✓ Branch clone complete: {}", targetDir);
        return targetDir;
    }

    /** Fallback: uses env token */
    public Path cloneBranch(String cloneUrl, String branch) throws Exception {
        return cloneBranch(cloneUrl, branch, null);
    }

    /**
     * Clone a repo and reset to a specific commit SHA.
     */
    public Path cloneAtCommit(String cloneUrl, String commitSha, String token) throws Exception {
        Path targetDir = Files.createTempDirectory(config.getCloneDir(), "repo-");
        log.info("Cloning {} at commit {} to {}", cloneUrl, commitSha.substring(0, 7), targetDir);

        String effectiveToken = resolveToken(token);
        Git git = Git.cloneRepository()
                .setURI(addTokenToUrl(cloneUrl, effectiveToken))
                .setDirectory(targetDir.toFile())
                .setCredentialsProvider(getCredentialsProvider(effectiveToken))
                .call();

        try {
            log.info("✓ Repo cloned, resetting to commit {}...", commitSha.substring(0, 7));
            git.reset()
                    .setMode(ResetCommand.ResetType.HARD)
                    .setRef(commitSha)
                    .call();
            git.clean()
                    .setCleanDirectories(true)
                    .setForce(true)
                    .call();
            log.info("✓ Commit checkout complete: {}", targetDir);
        } finally {
            git.close();
        }

        return targetDir;
    }

    /** Fallback: uses env token */
    public Path cloneAtCommit(String cloneUrl, String commitSha) throws Exception {
        return cloneAtCommit(cloneUrl, commitSha, null);
    }

    private boolean isCommitSha(String ref) {
        return ref != null && ref.matches(HEX_PATTERN);
    }

    public void cleanup(Path dir) {
        if (dir == null || !Files.exists(dir)) {
            return;
        }

        // On Windows, give JGit/JVM time to release file handles
        System.gc();
        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

        try (Stream<Path> walk = Files.walk(dir)) {
            walk.sorted(Comparator.reverseOrder())
                    .forEach(p -> {
                        try {
                            // Make file writable before deleting (Windows pack files are read-only)
                            File f = p.toFile();
                            if (!f.canWrite()) f.setWritable(true);
                            Files.delete(p);
                        } catch (IOException e) {
                            // Retry once after a short delay (Windows file lock)
                            try {
                                Thread.sleep(50);
                                Files.deleteIfExists(p);
                            } catch (Exception retry) {
                                log.debug("Could not delete: {}", p);
                            }
                        }
                    });
            log.info("Cleaned up: {}", dir);
        } catch (IOException e) {
            log.warn("Cleanup failed for {}: {}", dir, e.getMessage());
        }
    }

    private String resolveToken(String token) {
        if (token != null && !token.isEmpty()) return token;
        return config.getGithubToken();
    }

    private String addTokenToUrl(String url, String token) {
        if (token == null || token.isEmpty()) return url;
        if (url.startsWith("https://") && !url.contains("@")) {
            return url.replace("https://", "https://x-access-token:" + token + "@");
        }
        return url;
    }

    private UsernamePasswordCredentialsProvider getCredentialsProvider(String token) {
        if (token != null && !token.isEmpty()) {
            return new UsernamePasswordCredentialsProvider("x-access-token", token);
        }
        return null;
    }
}
