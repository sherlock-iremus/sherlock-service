package fr.cnrs.iremus.sherlock.xml.mei;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.XmlElement;

@Serdeable
public class MeiHead {
    private FileDesc fileDesc;

    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
    public FileDesc getFileDesc() {
        return this.fileDesc;
    }

    public void setFileDesc(FileDesc fileDesc) {
        this.fileDesc = fileDesc;
    }
}
