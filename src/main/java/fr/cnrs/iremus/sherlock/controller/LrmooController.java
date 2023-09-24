package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.pojo.Test;
import fr.cnrs.iremus.sherlock.pojo.lrmoo.FileUri;
import fr.cnrs.iremus.sherlock.service.ResourceService;
import fr.cnrs.iremus.sherlock.xml.mei.Mei;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.xml.bind.JAXB;
import jakarta.xml.bind.JAXBException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Controller("/api/lrmoo")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class LrmooController {
    @Inject
    ResourceService resourceService;

    @Get
    public Object index() {
        return new Test("Coucou");
    }

    @Post("/mei-file-uri")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse<Mei> createMinimalLrmooDatasetFromMeiUri(@Valid @Body FileUri body) throws JAXBException, IOException {
        URL meiFileUri = new URL(body.getFileUri());
        BufferedReader in = new BufferedReader(new InputStreamReader(meiFileUri.openStream()));

        Mei mei = JAXB.unmarshal(in, Mei.class);

        return HttpResponse.created(mei);
    }
}
