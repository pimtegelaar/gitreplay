package com.tegeltech.gitreplay.service;

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

    @Value("${upstreamBranch:master}")
    private String upstreamBranch;

    @Value("${localBranch:master}")
    private String localBranch;

    @Value("${repository.location}")
    private String repositoryLocation;

    @Value("${remote.url}")
    private String remoteUrl;

    @Autowired
    public BuildService(CommitRegistry commitRegistry, GitHelper gitHelper) {
        this.commitRegistry = commitRegistry;
        this.gitHelper = gitHelper;
    }

    public Optional<RevCommit> finished() throws IOException, GitAPIException, URISyntaxException {
        RevCommit nextCommit = commitRegistry.next();
        System.out.println("nextCommit is " + nextCommit);
        gitHelper.cherryPick(repositoryLocation, nextCommit);
        gitHelper.push(repositoryLocation, remoteUrl);
        return Optional.ofNullable(nextCommit);
    }

    public int init() throws IOException, GitAPIException {
        gitHelper.checkout(repositoryLocation, upstreamBranch);
        List<RevCommit> commits = gitHelper.listCommits(repositoryLocation);
        commits.forEach(commitRegistry::addCommit);
        gitHelper.checkout(repositoryLocation, localBranch);
        return commits.size();
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