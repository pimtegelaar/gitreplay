package com.tegeltech.gitreplay.buildserver;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BuildConfiguration {

    private String buildserverLocation;

    private String jobName;

    private String token;
}
