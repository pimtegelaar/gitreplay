package com.tegeltech.gitreplay.service;

import com.tegeltech.gitreplay.controller.domain.Configuration;
import com.tegeltech.gitreplay.git.CommitRegistry;
import com.tegeltech.gitreplay.git.GitHelper;
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
public class BuildService {

    private CommitRegistry commitRegistry;
    private GitHelper gitHelper;

    @Value("${repository.location}")
    private String repositoryLocation;

    @Value("${localBranch:master}")
    private String localBranch;

    @Value("${upstreamBranch:master}")
    private String upstreamBranch;


    @Autowired
    public BuildService(CommitRegistry commitRegistry, GitHelper gitHelper) {
        this.commitRegistry = commitRegistry;
        this.gitHelper = gitHelper;
    }

    public Optional<RevCommit> finished() throws IOException, GitAPIException, URISyntaxException {
        RevCommit nextCommit = commitRegistry.next();
        System.out.println("nextCommit is " + nextCommit);
        gitHelper.merge(repositoryLocation, nextCommit);
        gitHelper.push(repositoryLocation, localBranch);
        return Optional.ofNullable(nextCommit);
    }

    public int init(Configuration configuration) throws IOException, GitAPIException {
        updateConfiguration(configuration);
        gitHelper.checkout(repositoryLocation, upstreamBranch);
        List<RevCommit> commits = gitHelper.listCommits(repositoryLocation);
        commits.forEach(commitRegistry::addCommit);
        gitHelper.checkout(repositoryLocation, localBranch);
        return commits.size();
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