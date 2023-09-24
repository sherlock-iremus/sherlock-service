package fr.cnrs.iremus.sherlock.xml.mei;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.XmlElement;

@Serdeable
public class TitleStmt {
    private String title;

    @XmlElement(name = "title")
    public String getTitle() {
        return this.getTitle();
    }

    public void setTitle(String title) {
        this.title = title;
    }
}
