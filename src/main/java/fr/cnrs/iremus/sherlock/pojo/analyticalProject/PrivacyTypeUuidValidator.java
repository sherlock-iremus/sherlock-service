package fr.cnrs.iremus.sherlock.pojo.analyticalProject;

import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD})
@Constraint(validatedBy = {})
public @interface PrivacyTypeUuidValidator {
    String message() default "body.privacyTypeUuid does not match the specific pattern";
}
