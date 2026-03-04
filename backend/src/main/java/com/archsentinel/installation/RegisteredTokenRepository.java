package com.archsentinel.installation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface RegisteredTokenRepository extends JpaRepository<RegisteredToken, Long> {

    Optional<RegisteredToken> findByRepoFullName(String repoFullName);

    List<RegisteredToken> findByGithubUsername(String githubUsername);

    @Modifying
    @Transactional
    void deleteByRepoFullName(String repoFullName);

    boolean existsByRepoFullName(String repoFullName);
}


