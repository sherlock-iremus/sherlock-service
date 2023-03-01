package fr.cnrs.iremus.sherlock.pojo.analyticalEntity;

import fr.cnrs.iremus.sherlock.common.Triple;
import fr.cnrs.iremus.sherlock.pojo.e13.E13AsLinkToP141;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Introspected

public class NewAnalyticalEntity {

    /** AnalyticalEntity's subject */
    @NotEmpty
    private List<String> referredEntities;

    @NotBlank
    private String document_context;
    @NotBlank
    private String analytical_project;

    /** AnalyticalEntity's list of analysis */
    @NotEmpty
    private List<E13AsLinkToP141> e13s;

    public List<String> getReferredEntities() {
        return referredEntities;
    }

    public void setReferredEntities(List<String> referredEntity) {
        this.referredEntities = referredEntity;
    }

    public List<E13AsLinkToP141> getE13s() {
        return e13s;
    }

    public void setE13s(List<E13AsLinkToP141> e13s) {
        this.e13s = e13s;
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
