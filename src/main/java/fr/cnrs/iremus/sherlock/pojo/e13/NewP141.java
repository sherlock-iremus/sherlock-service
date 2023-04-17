package fr.cnrs.iremus.sherlock.pojo.e13;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotEmpty;
import java.util.List;

@Introspected
public class NewP141 {
    @NotEmpty
    private List<String> rdf_type;
    @NotEmpty
    private List<String> p2_type;

    private String p190;

    public List<String> getRdf_type() {
        return rdf_type;
    }

    public void setRdf_type(List<String> rdf_type) {
        this.rdf_type = rdf_type;
    }

    public List<String> getP2_type() {
        return p2_type;
    }

    public void setP2_type(List<String> p2_type) {
        this.p2_type = p2_type;
    }

    public String getP190() {
        return p190;
    }

    public void setP190(String p190) {
        this.p190 = p190;
    }
}
