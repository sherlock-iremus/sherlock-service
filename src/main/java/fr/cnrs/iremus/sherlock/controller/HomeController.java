package fr.cnrs.iremus.sherlock.controller;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.validation.constraints.NotNull;

@Controller("/api/")
@Tag(name = "1. Home")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class HomeController {

    @Produces(MediaType.TEXT_PLAIN)
    @Get
    public MutableHttpResponse<Object> index(@NotNull Authentication authentication) {
        return HttpResponse.ok(authentication.getAttributes().get("uuid"));
    }
}