package fr.cnrs.iremus.sherlock.pojo.analyticalProject;

import fr.cnrs.iremus.sherlock.pojo.user.config.UserColorValidator;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.serde.annotation.Serdeable;

@Introspected
@Serdeable
public class UpdateAnalyticalProject {
    private String label;
    private String description;
    @UserColorValidator
    private String color;
    @PrivacyTypeUuidValidator
    private String privacyTypeUuid;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public String getPrivacyTypeUuid() {
        return privacyTypeUuid;
    }

    public void setPrivacyTypeUuid(String privacyTypeUuid) {
        this.privacyTypeUuid = privacyTypeUuid;
    }
}
