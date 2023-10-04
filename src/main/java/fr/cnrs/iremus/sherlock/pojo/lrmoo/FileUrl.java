package fr.cnrs.iremus.sherlock.pojo.lrmoo;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;

@Introspected
@Serdeable
public class FileUrl {
    @NotBlank
    @JsonProperty("file_url")
    private final String fileUrl;

    public FileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getFileUrl() {
        return this.fileUrl;
    }
}
