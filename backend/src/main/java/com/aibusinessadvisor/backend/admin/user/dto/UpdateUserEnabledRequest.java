package com.aibusinessadvisor.backend.admin.user.dto;

import jakarta.validation.constraints.NotNull;


public record UpdateUserEnabledRequest(

        @NotNull
        Boolean enabled

) {
}