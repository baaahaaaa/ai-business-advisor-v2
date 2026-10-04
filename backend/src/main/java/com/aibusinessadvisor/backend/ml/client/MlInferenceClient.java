package com.aibusinessadvisor.backend.ml.client;

import com.aibusinessadvisor.backend.ml.config.MlApiProperties;
import com.aibusinessadvisor.backend.ml.dto.ClaimFrequencyMlResponse;
import com.aibusinessadvisor.backend.ml.dto.ClaimOccurrenceMlResponse;
import com.aibusinessadvisor.backend.ml.dto.FraudMlRequest;
import com.aibusinessadvisor.backend.ml.dto.FraudMlResponse;
import com.aibusinessadvisor.backend.ml.dto.InsuranceRiskMlRequest;
import com.aibusinessadvisor.backend.ml.dto.MlHealthResponse;
import com.aibusinessadvisor.backend.ml.exception.MlServiceException;

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
public class MlInferenceClient {

    private final HttpClient httpClient;
    private final JsonMapper jsonMapper;
    private final String baseUrl;
    private final Duration requestTimeout;


    public MlInferenceClient(
            MlApiProperties properties,
            JsonMapper jsonMapper
    ) {

        this.jsonMapper = jsonMapper;

        this.baseUrl = properties
                .getBaseUrl()
                .replaceAll("/+$", "");

        this.requestTimeout = Duration.ofSeconds(
                properties.getRequestTimeoutSeconds()
        );

        this.httpClient = HttpClient
                .newBuilder()
                .version(HttpClient.Version.HTTP_1_1)
                .connectTimeout(requestTimeout)
                .build();
    }


    public MlHealthResponse health() {

        HttpRequest request = HttpRequest
                .newBuilder()
                .uri(
                        URI.create(
                                baseUrl + "/health"
                        )
                )
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(requestTimeout)
                .header(
                        "Accept",
                        "application/json"
                )
                .GET()
                .build();

        return execute(
                request,
                MlHealthResponse.class
        );
    }


    public ClaimOccurrenceMlResponse predictClaimOccurrence(
            InsuranceRiskMlRequest requestBody
    ) {

        String jsonBody =
                serialize(
                        requestBody,
                        "claim occurrence"
                );

        HttpRequest request =
                buildJsonPostRequest(
                        "/v1/predict/claim-occurrence",
                        jsonBody
                );

        return execute(
                request,
                ClaimOccurrenceMlResponse.class
        );
    }


    public ClaimFrequencyMlResponse predictClaimFrequency(
            InsuranceRiskMlRequest requestBody
    ) {

        String jsonBody =
                serialize(
                        requestBody,
                        "claim frequency"
                );

        HttpRequest request =
                buildJsonPostRequest(
                        "/v1/predict/claim-frequency",
                        jsonBody
                );

        return execute(
                request,
                ClaimFrequencyMlResponse.class
        );
    }


    public FraudMlResponse predictFraud(
            FraudMlRequest requestBody
    ) {

        String jsonBody =
                serialize(
                        requestBody,
                        "fraud"
                );

        HttpRequest request =
                buildJsonPostRequest(
                        "/v1/predict/fraud",
                        jsonBody
                );

        return execute(
                request,
                FraudMlResponse.class
        );
    }


    private String serialize(
            Object requestBody,
            String operation
    ) {

        try {

            return jsonMapper.writeValueAsString(
                    requestBody
            );

        } catch (Exception exception) {

            throw new MlServiceException(
                    "Unable to serialize "
                            + operation
                            + " request",
                    exception
            );
        }
    }


    private HttpRequest buildJsonPostRequest(
            String path,
            String jsonBody
    ) {

        return HttpRequest
                .newBuilder()
                .uri(
                        URI.create(
                                baseUrl + path
                        )
                )
                .version(HttpClient.Version.HTTP_1_1)
                .timeout(requestTimeout)
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
    }


    private <T> T execute(
            HttpRequest request,
            Class<T> responseType
    ) {

        try {

            HttpResponse<String> response =
                    httpClient.send(
                            request,
                            HttpResponse.BodyHandlers.ofString(
                                    StandardCharsets.UTF_8
                            )
                    );

            if (
                    response.statusCode() < 200
                            || response.statusCode() >= 300
            ) {

                throw new MlServiceException(
                        "ML API returned HTTP "
                                + response.statusCode()
                                + ": "
                                + response.body()
                );
            }

            return jsonMapper.readValue(
                    response.body(),
                    responseType
            );

        } catch (InterruptedException exception) {

            Thread.currentThread().interrupt();

            throw new MlServiceException(
                    "ML API request was interrupted",
                    exception
            );

        } catch (IOException exception) {

            throw new MlServiceException(
                    "Unable to communicate with ML API",
                    exception
            );
        }
    }
}
