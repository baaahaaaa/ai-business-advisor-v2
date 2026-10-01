package com.aibusinessadvisor.backend.advisor.client;

import com.aibusinessadvisor.backend.advisor.config.AdvisorApiProperties;
import com.aibusinessadvisor.backend.advisor.dto.AdvisorResponse;
import com.aibusinessadvisor.backend.advisor.dto.FraudAdvisorRequest;
import com.aibusinessadvisor.backend.advisor.dto.RiskAdvisorRequest;
import com.aibusinessadvisor.backend.advisor.exception.AdvisorServiceException;

import org.springframework.stereotype.Component;

import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


@Component
public class AiAdvisorClient {

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final String baseUrl;


    public AiAdvisorClient(
            AdvisorApiProperties properties,
            JsonMapper jsonMapper
    ) {

        this.jsonMapper = jsonMapper;

        this.baseUrl = properties
                .getBaseUrl()
                .replaceAll("/+$", "");

        this.httpClient = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(Duration.ofSeconds(5))
                .build();
    }


    public AdvisorResponse explainRisk(
            RiskAdvisorRequest request
    ) {

        return post(
                "/v1/advisor/risk",
                request
        );
    }


    public AdvisorResponse explainFraud(
            FraudAdvisorRequest request
    ) {

        return post(
                "/v1/advisor/fraud",
                request
        );
    }


    private AdvisorResponse post(
            String path,
            Object requestBody
    ) {

        String jsonBody =
                serialize(requestBody);

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(
                        URI.create(
                                baseUrl + path
                        )
                )
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(Duration.ofSeconds(30))
                .header(
                        "Content-Type",
                        "application/json"
                )
                .header(
                        "Accept",
                        "application/json"
                )
                .POST(
                        HttpRequest.BodyPublishers
                                .ofString(
                                        jsonBody,
                                        StandardCharsets.UTF_8
                                )
                )
                .build();

        return execute(request);
    }


    private String serialize(
            Object requestBody
    ) {

        try {

            return jsonMapper
                    .writeValueAsString(
                            requestBody
                    );

        } catch (Exception exception) {

            throw new AdvisorServiceException(
                    "Unable to serialize advisor request",
                    exception
            );
        }
    }


    private AdvisorResponse execute(
            HttpRequest request
    ) {

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers
                                    .ofString(
                                            StandardCharsets.UTF_8
                                    )
                    );

            if (
                    response.statusCode() < 200
                            || response.statusCode() >= 300
            ) {

                throw new AdvisorServiceException(
                        "AI Advisor returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return jsonMapper.readValue(
                    response.body(),
                    AdvisorResponse.class
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new AdvisorServiceException(
                    "AI Advisor request interrupted",
                    exception
            );

        } catch (IOException exception) {

            throw new AdvisorServiceException(
                    "Unable to communicate with AI Advisor",
                    exception
            );
        }
    }
}
