package fr.cnrs.iremus.sherlock.pojo.analyticalProject;

import fr.cnrs.iremus.sherlock.pojo.user.config.ContributionGraphValidator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

@Introspected
@Serdeable
public class NewAnalyticalProject {

    @NotEmpty
    private String label;

    @ContributionGraphValidator
    @NotBlank
    private String contribution_graph;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getContribution_graph() {
        return contribution_graph;
    }

    public void setContribution_graph(String contribution_graph) {
        this.contribution_graph = contribution_graph;
    }
}
