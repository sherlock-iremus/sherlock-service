package fr.cnrs.iremus.sherlock.xml.mei;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.xml.bind.annotation.XmlAttribute;

@Serdeable
public class Test {
    private String test;

    @XmlAttribute
    public String getTest() {
        return this.test;
    }

    public void setTest(String test) {
        this.test = test;
    }
}
