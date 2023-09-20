package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.pojo.Error;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

@Controller("/test")
@Secured(SecurityRule.IS_ANONYMOUS)
public class TestController {
    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<Error> index() {
        Error o = new Error();
        o.setMessage("Hello World");
        return HttpResponse.ok(o);
    }
}