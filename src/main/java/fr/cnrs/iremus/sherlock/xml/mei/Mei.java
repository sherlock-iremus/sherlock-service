package fr.cnrs.iremus.sherlock.xml.mei;


import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.*;

@Serdeable
@XmlRootElement(name = "mei")
@XmlAccessorType(XmlAccessType.FIELD)
public class Mei {
    @XmlAttribute
    private String meiversion;

    public String getMeiversion() {
        return this.meiversion;
    }

    public void setMeiversion(String meiversion) {
        this.meiversion = meiversion;
    }

    @XmlElement(namespace = "http://www.music-encoding.org/ns/mei")
    private MeiHead meiHead;

    public MeiHead getMeiHead() {
        return this.meiHead;
    }

    public void setMeiHead(MeiHead meiHead) {
        this.meiHead = meiHead;
    }
}