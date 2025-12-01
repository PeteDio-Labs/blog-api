package com.petedillo.api.config;

import org.jetbrains.annotations.Nullable;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    @Nullable
    private String environment;
    @Nullable
    private String version;

    @Nullable
    public String getEnvironment() {
        return environment;
    }

    public void setEnvironment(@Nullable String environment) {
        this.environment = environment;
    }

    @Nullable
    public String getVersion() {
        return version;
    }

    public void setVersion(@Nullable String version) {
        this.version = version;
    }
}
