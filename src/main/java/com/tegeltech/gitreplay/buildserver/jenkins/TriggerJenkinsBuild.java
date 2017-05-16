package com.tegeltech.gitreplay.buildserver.jenkins;

import com.tegeltech.gitreplay.buildserver.BuildConfiguration;
import com.tegeltech.gitreplay.buildserver.TriggerBuildService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Slf4j
public class TriggerJenkinsBuild implements TriggerBuildService {

    private final OkHttpClient httpClient;

    @Value("${buildServer.location}")
    private String buildserverLocation;

    @Value("${buildServer.jobName}")
    private String jobName;

    @Value("${buildServer.token}")
    private String token;

    @Autowired
    public TriggerJenkinsBuild(OkHttpClient httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public void triggerBuild(BuildConfiguration configuration) {
        if(configuration != null) {
            updateConfiguration(configuration);
        }
        String url =  buildserverLocation +  "/job/" + jobName + "/build?token=" + token;
        Request request = new Request.Builder().url(url).build();
        try {
            Response response = httpClient.newCall(request).execute();
            int code = response.code();
            if(code == 201) {
                log.info("Next Jenkins build triggered.");
            } else {
                log.error("Next Jenkins build trigger failed with code: {}, message: {}", code, response.message());
            }
        } catch (IOException e) {
            throw new  RuntimeException("Trigger build failed", e);
        }
    }

    private void updateConfiguration(BuildConfiguration configuration)  {
        buildserverLocation = configuration.getBuildserverLocation();
        jobName = configuration.getJobName();
        token = configuration.getToken();
    }

    public static void main(String[] args) {
        TriggerJenkinsBuild triggerJenkinsBuild = new TriggerJenkinsBuild(new OkHttpClient.Builder().build());
        String buildserverLocation = "http://localhost:8080";
        String jobName = "commons-lang-full";
        String token = "6aa675ed03448a987b697123086a9c86";
        BuildConfiguration configuration = new BuildConfiguration(buildserverLocation, jobName, token);
        triggerJenkinsBuild.triggerBuild(configuration);
    }
}
