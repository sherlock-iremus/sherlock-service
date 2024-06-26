package fr.cnrs.iremus.sherlock.pojo.analyticalProject;

import fr.cnrs.iremus.sherlock.pojo.user.config.ContributionGraphValidator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserColorValidator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Introspected
@Serdeable
public class UpdateAnalyticalProject {
    private String label;
    private String description;
    @UserColorValidator
    private String color;
    @PrivacyTypeUuidValidator
    private String privacyTypeUuid;

    @ContributionGraphValidator
    @NotBlank
    private String contribution_graph;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPrivacyTypeUuid() {
        return privacyTypeUuid;
    }

    public void setPrivacyTypeUuid(String privacyTypeUuid) {
        this.privacyTypeUuid = privacyTypeUuid;
    }

    public String getContribution_graph() {
        return contribution_graph;
    }

    public void setContribution_graph(String contribution_graph) {
        this.contribution_graph = contribution_graph;
    }
}
