package com.tegeltech.gitreplay;

import com.tegeltech.gitreplay.git.GitHelper;
import com.tegeltech.gitreplay.git.RemoteAccess;
import org.eclipse.jgit.revwalk.RevCommit;

import java.util.List;

public class Main {
    public static void main(String[] args) throws Exception {
        RemoteAccess remoteAccess = new RemoteAccess();
        GitHelper gitHelper = new GitHelper(remoteAccess);
        List<RevCommit> commits = gitHelper.listCommits("/home/mutator/git/commons-lang/.git");
    }
}
