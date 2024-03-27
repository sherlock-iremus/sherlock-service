package fr.cnrs.iremus.sherlock.service;

import io.micronaut.http.MutableHttpResponse;
import org.apache.jena.rdf.model.Model;

public class SherlockServiceException extends Exception {
    private final MutableHttpResponse <String> httpResponse;
    private final String reason;
    private final Model model;

    SherlockServiceException(MutableHttpResponse
                                      <String> httpResponse) {
        this.httpResponse = httpResponse;
        this.reason = "";
        this.model = null;
    }

    public SherlockServiceException(String reason, Model model, MutableHttpResponse<String> httpResponse) {
        this.httpResponse = httpResponse;
        this.reason = reason;
        this.model = model;
    }

    public MutableHttpResponse<String> getHttpResponse() {
        return httpResponse;
    }

    public String getReason() {
        return reason;
    }

    public Model getModel() {
        return model;
    }
}
