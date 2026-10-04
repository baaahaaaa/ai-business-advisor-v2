package com.aibusinessadvisor.backend.advisor.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Component
@ConfigurationProperties(prefix = "advisor-api")
public class AdvisorApiProperties {

    private String baseUrl;
    private int requestTimeoutSeconds;


    public String getBaseUrl() {
        return baseUrl;
    }


    public void setBaseUrl(
            String baseUrl
    ) {
        this.baseUrl = baseUrl;
    }


    public int getRequestTimeoutSeconds() {
        return requestTimeoutSeconds;
    }


    public void setRequestTimeoutSeconds(
            int requestTimeoutSeconds
    ) {
        this.requestTimeoutSeconds =
                requestTimeoutSeconds;
    }
}
