package fr.cnrs.iremus.sherlock.common;

import com.fasterxml.jackson.annotation.JsonValue;
import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public enum ResourceType {
    URI("URI"),
    LITERAL("LITERAL"),
    NEW_RESOURCE("NEW_RESOURCE");

    @JsonValue
    private final String label;

    ResourceType(String label) {
        this.label = label;
    }
}
