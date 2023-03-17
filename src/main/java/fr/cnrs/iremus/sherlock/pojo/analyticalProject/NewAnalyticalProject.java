package fr.cnrs.iremus.sherlock.pojo.analyticalProject;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotEmpty;

@Introspected
public class NewAnalyticalProject {

    @NotEmpty
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
