package com.tegeltech.gitreplay.git;

import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Component
@Slf4j
public class CommitRegistry {

    private List<RevCommit> commits = new ArrayList<>();

    private RevCommit currentCommit = null;

    public void clear() {
        commits.clear();
    }

    public void addCommit(RevCommit commit) {
        commits.add(commit);
    }

    public RevCommit next() {
        int index = commits.indexOf(currentCommit);
        if(index == -1)  {
            log.info("Couldn't find current commit: {} {}", currentCommit.getId(), currentCommit.getShortMessage());
            return null;
        }
        index++;
        if (index >= commits.size()) {
            log.info("Reached the end of the commit stream");
            return null;
        }
        currentCommit = commits.get(index);
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

    public List<RevCommit> getCommits() {
        return commits;
    }
}
