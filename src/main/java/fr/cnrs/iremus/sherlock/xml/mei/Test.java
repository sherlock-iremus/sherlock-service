package fr.cnrs.iremus.sherlock.xml.mei;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@Serdeable
@XmlAccessorType(XmlAccessType.FIELD)
public class Test {
    @XmlAttribute
    private String test;

    public String getTest() {
        return this.test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
