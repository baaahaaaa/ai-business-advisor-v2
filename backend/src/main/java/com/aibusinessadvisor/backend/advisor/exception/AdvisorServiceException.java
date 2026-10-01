package com.aibusinessadvisor.backend.advisor.exception;


public class AdvisorServiceException
        extends RuntimeException {

    public AdvisorServiceException(
            String message
    ) {
        super(message);
    }


    public AdvisorServiceException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
