package com.tegeltech.gitreplay.configuration;

import okhttp3.OkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfiguration {

    @Bean
    public OkHttpClient  okHttpClient() {
        return new OkHttpClient();
    }
}
