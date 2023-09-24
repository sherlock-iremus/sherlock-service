package fr.cnrs.iremus.sherlock.pojo;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Test {
    private final String message;

    public Test(String message) {
        this.message = message;
    }

    public String getMessage() {
        return this.message;
    }
}
