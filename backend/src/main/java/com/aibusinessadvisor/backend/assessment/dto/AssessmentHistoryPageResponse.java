package com.aibusinessadvisor.backend.assessment.dto;

import java.util.List;

public record AssessmentHistoryPageResponse(

        List<AssessmentHistoryItemResponse> items,

        int page,

        int size,

        long totalElements,

        int totalPages,

        boolean hasNext,

        boolean hasPrevious

) {
}