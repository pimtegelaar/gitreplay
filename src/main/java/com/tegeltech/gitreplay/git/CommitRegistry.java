package com.tegeltech.gitreplay.git;

import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
public class CommitRegistry {

    private List<RevCommit> commits = new ArrayList<>();

    private RevCommit currentCommit = null;

    public void addCommit(RevCommit commit) {
        commits.add(commit);
    }

    public RevCommit next() {
        int index = commits.indexOf(currentCommit);
        if (index == -1 | index > commits.size() + 1) {
            return null;
        }
        currentCommit = commits.get(index + 1);
        return currentCommit;
    }

    public void setCurrentCommit(RevCommit currentCommit) {
        this.currentCommit = currentCommit;
    }

    public void setCurrentCommit(int index) {
        setCurrentCommit(commits.get(index));
    }

    public RevCommit getCurrentCommit() {
        return currentCommit;
    }

    public Optional<RevCommit> getCommit(String commitHash) {
        return commits.stream().filter(commit -> commit.getId().getName().equals(commitHash)).findAny();
    }
}
