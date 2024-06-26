package fr.cnrs.iremus.sherlock.pojo.e13;

import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.pojo.user.config.ContributionGraphValidator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserColorValidator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Introspected
@Serdeable
@NewE13P141Validator
public class NewE13 {
    @NotEmpty
    private List<String> p140;
    @NotBlank
    private String p177;
    private String p141;
    @Valid
    private NewP141 new_p141;
    @NotBlank
    private String document_context;
    @ContributionGraphValidator
    @NotBlank
    private String contribution_graph;
    @NotBlank
    private String analytical_project;
    @NotNull
    private ResourceType p141_type;

    public List<String> getP140() {
        return p140;
    }

    public void setP140(List<String> p140) {
        this.p140 = p140;
    }

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

    public NewP141 getNew_p141() {
        return new_p141;
    }

    public void setNew_p141(NewP141 new_p141) {
        this.new_p141 = new_p141;
    }

    public String getContribution_graph() {
        return contribution_graph;
    }

    public void setContribution_graph(String contribution_graph) {
        this.contribution_graph = contribution_graph;
    }
}
