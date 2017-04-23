package com.tegeltech.gitreplay.controller.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class Commit {

    private String name;
    private String message;

}
