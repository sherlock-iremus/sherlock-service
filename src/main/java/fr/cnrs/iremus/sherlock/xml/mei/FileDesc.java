package fr.cnrs.iremus.sherlock.xml.mei;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.XmlElement;

@Serdeable
public class FileDesc {
    private TitleStmt titleStmt;

    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
    public TitleStmt getTitleStmt() {
        return this.titleStmt;
    }

    public void setTitleStmt(TitleStmt titleStmt) {
        this.titleStmt = titleStmt;
    }
}