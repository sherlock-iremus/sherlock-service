package fr.cnrs.iremus.sherlock.pojo.analyticalEntity;

import fr.cnrs.iremus.sherlock.pojo.e13.E13AsLinkToP141;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Introspected

public class NewAnalyticalEntity {

    /** AnalyticalEntity's type */
    @NotBlank
    private String p177;

    /** AnalyticalEntity's subject */
    @NotBlank
    private String p140;

    /** AnalyticalEntity's list of analysis */
    private List<E13AsLinkToP141> e13s;

    public String getP177() {
        return p177;
    }

    public void setP177(String p177) {
        this.p177 = p177;
    }

    public String getP140() {
        return p140;
    }

    public void setP140(String p140) {
        this.p140 = p140;
    }

    public List<E13AsLinkToP141> getE13s() {
        return e13s;
    }

    public void setE13s(List<E13AsLinkToP141> e13s) {
        this.e13s = e13s;
    }
}
