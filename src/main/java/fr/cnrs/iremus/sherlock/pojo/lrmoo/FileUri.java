package fr.cnrs.iremus.sherlock.pojo.lrmoo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Introspected
@Serdeable
public class FileUri {
    @NotBlank
    @JsonProperty
    private final String fileUri;

    public FileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public String getFileUri() {
        return this.fileUri;
    }
}
