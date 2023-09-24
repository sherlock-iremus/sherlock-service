package fr.cnrs.iremus.sherlock.xml.mei;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@Serdeable
@XmlType
public class MeiHead {
    private FileDesc fileDesc;

    @XmlElement
    public FileDesc getFileDesc() {
        return this.fileDesc;
    }

    public void setFileDesc(FileDesc fileDesc) {
        this.fileDesc = fileDesc;
    }
}
