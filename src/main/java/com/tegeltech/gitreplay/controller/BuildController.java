package com.tegeltech.gitreplay.controller;

import com.tegeltech.gitreplay.controller.domain.Commit;
import com.tegeltech.gitreplay.service.BuildService;
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

    @RequestMapping(value = "/commits", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Commit> getCommits() {
        return buildService.getCommits().stream()
                .map(commit -> new Commit(commit.getName(), commit.getFullMessage()))
                .collect(Collectors.toList());
//        return Arrays.asList(new Commit("0987a6e9876ea9876c987ae6ce8a76d","added some stuff"),new Commit("6876a7e698a7e698a76","my commit message"));
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
    public Commit setCurrentCommit(@PathVariable String name) {
        Optional<RevCommit> commit = buildService.setCurrentCommit(name);
        return commit.map(revCommit -> new Commit(revCommit.getName(), revCommit.getFullMessage())).orElse(null);
    }

    @RequestMapping(value = "/current-commit", method = RequestMethod.GET)
    public Commit getCurrentCommit() {
        Optional<RevCommit> commit = buildService.getCurrentCommit();
        return commit.map(revCommit -> new Commit(revCommit.getName(), revCommit.getFullMessage())).orElse(null);
    }

}
