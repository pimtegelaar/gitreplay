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
import java.util.Iterator;
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

        Iterable<RevCommit> logs = git.log().call();
        Iterator<RevCommit> commitIterator = logs.iterator();
        RevCommit firstCommit = commitIterator.next();
        while (commitIterator.hasNext()) {
            commitIterator.next();
        }
        List<RevCommit> commits = commits(firstCommit, new ArrayList<>());
        Collections.reverse(commits);
        return commits;
    }

    private List<RevCommit> commits(RevCommit commit, List<RevCommit> commits) {
        commits.add(commit);
        RevCommit[] parents = commit.getParents();
        if (parents == null || parents.length == 0) return commits;
        RevCommit parent = parents[0];
        return commits(parent, commits);
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
        git.cherryPick().setMainlineParentNumber(1).include(commit).call();
    }


    public void merge(String repositoryLocation, RevCommit commit) throws IOException, GitAPIException {
        Repository db = repo(repositoryLocation);
        Git git = new Git(db);
        git.merge().include(commit).call();
    }



    public Iterable<PushResult> push(String repositoryLocation, String branchName) throws IOException, URISyntaxException, GitAPIException {
        Repository db = repo(repositoryLocation);
        Git git = new Git(db);
//        Config config = config(remoteUrl);

        String remote = "origin";
        String branch = "refs/heads/" + branchName;

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
