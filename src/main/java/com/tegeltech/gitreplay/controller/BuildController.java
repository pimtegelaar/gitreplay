package com.tegeltech.gitreplay.controller;

import com.tegeltech.gitreplay.controller.domain.Commit;
import com.tegeltech.gitreplay.controller.domain.Configuration;
import com.tegeltech.gitreplay.service.BuildService;
import com.tegeltech.gitreplay.service.ReplayStatus;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/build")
@CrossOrigin
@Slf4j
public class BuildController {

    private final BuildService buildService;

    @Autowired
    public BuildController(BuildService buildService) {
        this.buildService = buildService;
    }

    @RequestMapping(value = "/finished", method = RequestMethod.POST)
    public String buildFinished() {
        try {
            Optional<RevCommit> commit = buildService.finished();
            if (!commit.isPresent()) {
                String message = "End of commit stream reached!";
                log.info(message);
                return message;
            }
            String commitHash = commit.get().getId().name();
            String commitMessage = commit.get().getFullMessage();
            String message = String.format("Pushed next commit: %s, with message: %s", commitHash, commitMessage);
            log.info(message);
            return message;
        } catch (IOException | GitAPIException | URISyntaxException e) {
            String message = "Failed with exception: ";
            log.error(message, e);
            return message + e;
        }
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public String init(@RequestBody(required = false) Configuration configuration) {
        try {
            if (configuration == null) configuration = new Configuration();
            int commits = buildService.init(configuration);
            String message = String.format("Found %s commits", commits);
            log.info(message);
            return message;
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            return "Failed with exception: " + e;
        }
    }

    @RequestMapping(value = "/commits", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Commit> getCommits() {
        log.info("Retrieving commits...");
        List<Commit> commits = buildService.getCommits().stream()
                .map(commit -> new Commit(commit.getName(), commit.getFullMessage()))
                .collect(Collectors.toList());
        log.info("Found {} commits", commits.size());
        return commits;
    }

    @RequestMapping(value = "/commit/{name}", method = RequestMethod.GET)
    public String getCommit(@PathVariable String name) {
        Optional<RevCommit> commit = buildService.getCommit(name);
        if (!commit.isPresent()) {
            String message = String.format("Commit %s not found", name);
            log.info(message);
            return message;
        }
        String message = String.format("Found commit: %s, with message: %s", name, commit.get().getFullMessage());
        log.info(message);
        return message;
    }

    @RequestMapping(value = "/current-commit/{name}", method = RequestMethod.POST)
    public Commit setCurrentCommit(@PathVariable String name) {
        Optional<RevCommit> commit = buildService.setCurrentCommit(name);
        log.info("Setting {} as next commit ", name);
        return commit.map(revCommit -> new Commit(revCommit.getName(), revCommit.getFullMessage())).orElse(null);
    }

    @RequestMapping(value = "/current-commit", method = RequestMethod.GET)
    public Commit getCurrentCommit() {
        log.info("Retrieving current commit");
        Optional<RevCommit> commit = buildService.getCurrentCommit();
        return commit.map(revCommit -> new Commit(revCommit.getName(), revCommit.getFullMessage())).orElse(null);
    }

    @RequestMapping(value = "/pause-resume", method = RequestMethod.PUT)
    public ReplayStatus pauseResume() {
        ReplayStatus replayStatus = buildService.pauseResume();
        log.info("Replay status is now {}", replayStatus);
        return replayStatus;
    }

}
