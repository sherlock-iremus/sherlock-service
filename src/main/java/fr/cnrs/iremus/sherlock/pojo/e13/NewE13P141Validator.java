package fr.cnrs.iremus.sherlock.pojo.e13;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.Constraint;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Constraint(validatedBy = {})
@Serdeable
public @interface NewE13P141Validator {
    public static final String PLEASE_SET_P141 = "Please set either body.p141 or body.new_p141. And set corresponding p141_type";

    String message() default PLEASE_SET_P141;
}
