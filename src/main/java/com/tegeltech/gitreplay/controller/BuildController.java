package com.tegeltech.gitreplay.controller;

import com.tegeltech.gitreplay.service.BuildService;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.revwalk.RevCommit;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Optional;

@RestController
@RequestMapping("/build")
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
                return "End of commit stream reached!";
            }
            return String.format("Pushed next commit: %s, with message: %s", commit.get().getId().name(), commit.get().getFullMessage());
        } catch (IOException | GitAPIException | URISyntaxException e) {
            e.printStackTrace();
            return "Failed with exception: " + e;
        }
    }

    @RequestMapping(value = "/init", method = RequestMethod.POST)
    public String init() {
        try {
            int commits = buildService.init();
            return String.format("Found %s commits", commits);
        } catch (IOException | GitAPIException e) {
            e.printStackTrace();
            return "Failed with exception: " + e;
        }
    }

    @RequestMapping(value = "/commit/{name}", method = RequestMethod.GET)
    public String getCommit(@PathVariable String name) {
        Optional<RevCommit> commit = buildService.getCommit(name);
        if (!commit.isPresent()) {
            return String.format("Commit %s not found", name);
        }
        return String.format("Found commit: %s, with message: %s", name, commit.get().getFullMessage());
    }

    @RequestMapping(value = "/current-commit/{name}", method = RequestMethod.POST)
    public String setCurrentCommit(@PathVariable String name) {
        Optional<RevCommit> commit = buildService.setCurrentCommit(name);
        if (!commit.isPresent()) {
            return String.format("Commit %s not found", name);
        }
        return String.format("Current commit is now: %s, with message: %s", name, commit.get().getFullMessage());
    }

    @RequestMapping(value = "/current-commit", method = RequestMethod.GET)
    public String getCurrentCommit() {
        Optional<RevCommit> commit = buildService.getCurrentCommit();
        if (!commit.isPresent()) {
            return "No current commit set";
        }
        return String.format("Current commit is now: %s, with message: %s", commit.get().getId().name(), commit.get().getFullMessage());
    }

}
