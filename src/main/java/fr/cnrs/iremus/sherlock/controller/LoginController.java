package fr.cnrs.iremus.sherlock.controller;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.views.View;
import io.swagger.v3.oas.annotations.Hidden;

import java.util.HashMap;
import java.util.Map;

@Controller("/login")
@Hidden
public class LoginController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @View("login")
    @Get
    public Map<String, Object> login() {
        return new HashMap<>();
    }

    @Secured(SecurityRule.IS_AUTHENTICATED)
    @View("redirect")
    @Get("/redirect")
    public Map<String, Object> redirect() {
        return new HashMap<>();
    }
}
