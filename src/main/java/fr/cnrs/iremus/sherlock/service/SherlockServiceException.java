package fr.cnrs.iremus.sherlock.service;

import io.micronaut.http.MutableHttpResponse;

public class SherlockServiceException extends Exception {
    private final MutableHttpResponse <String> httpResponse;

    SherlockServiceException(MutableHttpResponse
                                      <String> httpResponse) {
        this.httpResponse = httpResponse;
    }

    public MutableHttpResponse<String> getHttpResponse() {
        return httpResponse;
    }
}
