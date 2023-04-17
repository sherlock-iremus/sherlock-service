package fr.cnrs.iremus.sherlock.pojo.e90;

import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import java.util.List;

@Introspected
public class NewE90Fragment {
    @NotBlank
    private String parent;
    @NotEmpty
    private List<String> p2_type;

    public String getParent() {
        return parent;
    }

    public void setParent(String parent) {
        this.parent = parent;
    }

    public List<String> getP2_type() {
        return p2_type;
    }

    public void setP2_type(List<String> p2_type) {
        this.p2_type = p2_type;
    }
}
