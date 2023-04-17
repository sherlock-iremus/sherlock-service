package fr.cnrs.iremus.sherlock.common;

import fr.cnrs.iremus.sherlock.pojo.e13.NewE13;
import fr.cnrs.iremus.sherlock.pojo.e13.NewE13P141Validator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserColorValidator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfigEdit;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfigValidator;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserEmojiValidator;
import fr.cnrs.iremus.sherlock.service.ValidatorService;
import io.micronaut.context.annotation.Factory;
import io.micronaut.validation.validator.constraints.ConstraintValidator;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

@Factory
public class ValidatorFactory {
    @Inject
    ValidatorService validatorService;

    @Singleton
    ConstraintValidator<UserConfigValidator, UserConfigEdit> userConfigValidator() {
        return (value, annotationMetadata, context) -> {
            assert value != null;
            return value.getEmoji() != null || value.getColor() != null;
        };
    }

    @Singleton
    ConstraintValidator<UserEmojiValidator, String> userEmojiValidator() {
        return (value, annotationMetadata, context) -> value == null || validatorService.isUnicodePattern(value);
    }

    @Singleton
    ConstraintValidator<UserColorValidator, String> userColorValidator() {
        return (value, annotationMetadata, context) -> value == null || validatorService.isHexColorCode(value);
    }

    @Singleton
    ConstraintValidator<NewE13P141Validator, NewE13> newE13P141Validator() {
        return (value, annotationMetadata, context) -> {
            assert value != null;
            return hasE13ValidNewP141(value) || hasE13ValidP141(value);
        };
    }

    private boolean hasE13ValidNewP141 (NewE13 newE13) {
        return newE13.getNew_p141() != null &&
                newE13.getP141() == null &&
                newE13.getP141_type().equals(ResourceType.NEW_RESOURCE);
    }

    private boolean hasE13ValidP141 (NewE13 newE13) {
        return newE13.getNew_p141() == null &&
                newE13.getP141() != null &&
                (newE13.getP141_type().equals(ResourceType.URI) || newE13.getP141_type().equals(ResourceType.LITERAL));
    }
}