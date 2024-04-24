package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.controller.AnalyticalProjectController;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Singleton
public class ValidatorService {

    private final Pattern hexColorPattern = Pattern.compile("^([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
    private final Pattern unicodePattern = Pattern.compile("^.$");
    private Matcher matcher;

    @Inject
    private Sherlock sherlock;

    public boolean isHexColorCode(final String hexColorCode) {
        matcher = hexColorPattern.matcher(hexColorCode);
        return matcher.matches();
    }

    public boolean isUnicodePattern(final String unicodeChar) {
        matcher = unicodePattern.matcher(unicodeChar);
        return matcher.matches();
    }

    public boolean isPrivacyTypeUuid(String value) {
        return value.equals(sherlock.getUuidFromSherlockUri(AnalyticalProjectController.e55draftIri))
        || value.equals(sherlock.getUuidFromSherlockUri(AnalyticalProjectController.e55publishedIri));
    }
}
