package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.lrmoo.FileUrl;
import fr.cnrs.iremus.sherlock.service.ResourceService;
import fr.cnrs.iremus.sherlock.xml.mei.Mei;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.xml.bind.JAXB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;

@Controller("/api/mei")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class MeiController {
    @Inject
    ResourceService resourceService;

    @Inject
    Sherlock sherlock;

    @Post("/head")
    @Produces(MediaType.APPLICATION_JSON)
    @Secured(SecurityRule.IS_ANONYMOUS)
    public MutableHttpResponse<Mei> parseMeiHead(@Valid @Body FileUrl body) throws IOException {
        URL meiFileUrl = new URL(body.getFileUrl());
        Mei mei;
        try {
            mei = JAXB.unmarshal(new BufferedReader(new InputStreamReader(meiFileUrl.openStream(), StandardCharsets.UTF_8)), Mei.class);
        } catch (Exception e) {
            mei = JAXB.unmarshal(new BufferedReader(new InputStreamReader(meiFileUrl.openStream(), StandardCharsets.UTF_16)), Mei.class);
        }

        return HttpResponse.ok(mei);
    }
}
