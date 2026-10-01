package com.aibusinessadvisor.backend.advisor.dto;

import java.util.List;


public record AdvisorResponse(

        String title,

        String summary,

        List<String> keyPoints,

        String disclaimer

) {
}
