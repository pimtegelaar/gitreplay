package com.tegeltech.gitreplay.service;

import com.tegeltech.gitreplay.controller.domain.Configuration;
import com.tegeltech.gitreplay.git.CommitRegistry;
import com.tegeltech.gitreplay.git.GitHelper;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class BuildService {

    private CommitRegistry commitRegistry;
    private GitHelper gitHelper;

    @Value("${repository.location}")
    private String repositoryLocation;

    @Value("${localBranch:master}")
    private String localBranch;

    @Value("${upstreamBranch:master}")
    private String upstreamBranch;

    private ReplayStatus status = ReplayStatus.NONE;
    private ReplayStatus previousStatus = ReplayStatus.NONE;

    @Autowired
    public BuildService(CommitRegistry commitRegistry, GitHelper gitHelper) {
        this.commitRegistry = commitRegistry;
        this.gitHelper = gitHelper;
    }

    public Optional<RevCommit> finished() throws IOException, GitAPIException, URISyntaxException {
        if(status.equals(ReplayStatus.PAUSED)) {
            return Optional.empty();
        }
        RevCommit nextCommit = commitRegistry.next();
        if(nextCommit == null) {
            log.info("reached end of stream, finished :)");
            status =  ReplayStatus.FINISHED;
            return Optional.empty();
        }
        status = ReplayStatus.RUNNING;
        log.info("nextCommit is {}", nextCommit);
        gitHelper.merge(repositoryLocation, nextCommit);
        gitHelper.push(repositoryLocation, localBranch);
        return Optional.of(nextCommit);
    }

    public int init(Configuration configuration) throws IOException, GitAPIException {
        status = ReplayStatus.INITIALIZING;
        updateConfiguration(configuration);
        log.info("Checkout of repository {} upstream branch {}", repositoryLocation, upstreamBranch);
        gitHelper.checkout(repositoryLocation, upstreamBranch);
        log.info("Loading commits...");
        List<RevCommit> commits = gitHelper.listCommits(repositoryLocation);
        log.info("Found {} commits", commits.size());
        commitRegistry.clear();
        commits.forEach(commitRegistry::addCommit);
        log.info("Checkout of repository {} local branch {}", repositoryLocation, localBranch);
        gitHelper.checkout(repositoryLocation, localBranch);
        status = ReplayStatus.INITIALIZED;
        return commits.size();
    }

    public ReplayStatus pauseResume() {
        if (status.equals(ReplayStatus.PAUSED)) {
            status = previousStatus;
        } else  {
            previousStatus = status;
            status = ReplayStatus.PAUSED;
        }
        return status;
    }

    private void updateConfiguration(Configuration configuration) {
        String repositoryLocation = configuration.getRepositoryLocation();
        if (repositoryLocation != null)
            this.repositoryLocation = repositoryLocation;
        String localBranch = configuration.getLocalBranch();
        if (localBranch != null)
            this.localBranch = localBranch;
        String upstreamBranch = configuration.getUpstreamBranch();
        if (upstreamBranch != null)
            this.upstreamBranch = upstreamBranch;
    }

    public List<RevCommit> getCommits() {
        return commitRegistry.getCommits();
    }

    public Optional<RevCommit> getCommit(String commitHash) {
        return commitRegistry.getCommit(commitHash);
    }

    public Optional<RevCommit> getCurrentCommit() {
        return Optional.ofNullable(commitRegistry.getCurrentCommit());
    }

    public Optional<RevCommit> setCurrentCommit(String commitHash) {
        Optional<RevCommit> commit = getCommit(commitHash);
        commit.ifPresent(revCommit -> commitRegistry.setCurrentCommit(revCommit));
        return commit;
    }
}