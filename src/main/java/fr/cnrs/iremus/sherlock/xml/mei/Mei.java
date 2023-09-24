package fr.cnrs.iremus.sherlock.xml.mei;


import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

@Serdeable
@XmlRootElement(name = "mei")
public class Mei {
    private String meiversion;

    @XmlAttribute
    public String getMeiversion() {
        return this.meiversion;
    }

    public void setMeiversion(String meiversion) {
        this.meiversion = meiversion;
    }

    private MeiHead meiHead;

    @XmlElement
    public MeiHead getMeiHead() {
        return this.meiHead;
    }

    public void setMeiHead(MeiHead meiHead) {
        this.meiHead = meiHead;
    }

    private Test test;

    @XmlElement
    public Test getTest() {
        return this.test;
    }

    public void setTest(Test test) {
        this.test = test;
    }

}