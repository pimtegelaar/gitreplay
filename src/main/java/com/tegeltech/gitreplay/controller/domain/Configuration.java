package com.tegeltech.gitreplay.controller.domain;

import lombok.Data;

@Data
public class Configuration {

    private String repositoryLocation;

    private String localBranch;

    private String upstreamBranch;
}
