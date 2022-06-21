package fr.cnrs.iremus.sherlock.pojo.selection;

import java.util.List;

import javax.validation.constraints.NotEmpty;
import io.micronaut.core.annotation.Introspected;

@Introspected
public class SelectionCreate {

    public static String e55SelectionTypeIri = "http://data-iremus.huma-num.fr/id/9d0388cb-a178-46b2-b047-b5a98f7bdf0b";
    @NotEmpty
    private List<String> document_contexts;
    @NotEmpty
    private List<String> children;

    public List<String> getChildren() {
        return children;
    }
    public void setChildren(List<String> children) {
        this.children = children;
    }

    public List<String> getDocument_contexts() {
        return document_contexts;
    }

    public void setDocument_contexts(List<String> document_contexts) {
        this.document_contexts = document_contexts;
    }
}
