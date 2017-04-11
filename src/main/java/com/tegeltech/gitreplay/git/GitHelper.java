package com.tegeltech.gitreplay.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Config;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Component
public class GitHelper {

    private RemoteAccess remoteAccess;

    @Autowired
    public GitHelper(RemoteAccess remoteAccess) {
        this.remoteAccess = remoteAccess;
    }

    public List<RevCommit> listCommits(String repositoryLocation) throws IOException, GitAPIException {
        Repository repo = repo(repositoryLocation);
        Git git = new Git(repo);

        Iterable<RevCommit> logs = git.log()
                .call();
        List<RevCommit> commits = new ArrayList<>();
        logs.forEach(commits::add);
        Collections.reverse(commits);
        return commits;
    }

    public void checkout(String repositoryLocation, String branchName) throws IOException, GitAPIException {
        Repository repo = repo(repositoryLocation);
        Git git = new Git(repo);
        git.checkout().setName(branchName).call();
    }

    private Repository repo(String repositoryLocation) throws IOException {
        File gitDir = new File(repositoryLocation);
        return new FileRepositoryBuilder().setGitDir(gitDir).build();
    }

    public void cherryPick(String repositoryLocation, RevCommit commit) throws IOException, GitAPIException {
        Repository db = repo(repositoryLocation);
        Git git = new Git(db);
        git.cherryPick().include(commit).call();
    }

    public Iterable<PushResult> push(String repositoryLocation, String remoteUrl) throws IOException, URISyntaxException, GitAPIException {
        Repository db = repo(repositoryLocation);
        Git git = new Git(db);
//        Config config = config(remoteUrl);

        String remote = "origin";
        String branch = "refs/heads/master";

//        RemoteConfig remoteConfig = remoteConfig(db, remote, config);

        RefSpec spec = new RefSpec(branch + ":" + branch);
        CredentialsProvider credentialsProvider = remoteAccess.getCredentialsProvider();
        return git.push().setRemote(remote).setCredentialsProvider(credentialsProvider)
                .setRefSpecs(spec).call();
    }

    private RemoteConfig remoteConfig(Repository db, String remote, Config config) throws URISyntaxException, MalformedURLException {
        URIish uri = new URIish(db.getDirectory().toURI().toURL());
        RemoteConfig remoteConfig = new RemoteConfig(config, "origin");
        remoteConfig.addURI(uri);
        remoteConfig.addFetchRefSpec(new RefSpec("+refs/heads/*:refs/remotes/"
                + remote + "/*"));
        remoteConfig.update(config);
        return remoteConfig;
    }

    private Config config(String remoteUrl) {
        Config config = new Config();
        config.setString("remote", "origin", "pushurl", "short:project.git");
        config.setString("url", remoteUrl, "name", "short:");
        return config;
    }
}
