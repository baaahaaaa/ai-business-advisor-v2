package com.aibusinessadvisor.backend.ml.dto;

import java.util.Map;


public record MlHealthResponse(
        String status,
        Map<String, Boolean> models
) {
}
