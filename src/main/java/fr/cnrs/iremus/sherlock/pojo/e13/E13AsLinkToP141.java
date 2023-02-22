package fr.cnrs.iremus.sherlock.pojo.e13;

import fr.cnrs.iremus.sherlock.common.ResourceType;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Introspected
public class E13AsLinkToP141 {
    @NotBlank
    private String p177;
    @NotBlank
    private String p141;
    @NotNull
    private ResourceType p141_type;
    @NotBlank
    private String document_context;
    @NotBlank
    private String analytical_project;
    public String getP177() {
        return p177;
    }

    public void setP177(String p177) {
        this.p177 = p177;
    }

    public String getP141() {
        return p141;
    }

    public void setP141(String p141) {
        this.p141 = p141;
    }

    public ResourceType getP141_type() {
        return p141_type;
    }

    public void setP141_type(ResourceType p141_type) {
        this.p141_type = p141_type;
    }

    public String getDocument_context() {
        return document_context;
    }

    public void setDocument_context(String document_context) {
        this.document_context = document_context;
    }

    public String getAnalytical_project() {
        return analytical_project;
    }

    public void setAnalytical_project(String analytical_project) {
        this.analytical_project = analytical_project;
    }
}
