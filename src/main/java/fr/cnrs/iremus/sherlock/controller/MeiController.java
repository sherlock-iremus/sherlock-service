package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.Test;
import fr.cnrs.iremus.sherlock.pojo.lrmoo.FileUrl;
import fr.cnrs.iremus.sherlock.service.ResourceService;
import fr.cnrs.iremus.sherlock.xml.mei.Mei;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.xml.bind.JAXB;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;

@Controller("/api/mei")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class MeiController {
    @Inject
    ResourceService resourceService;

    @Inject
    Sherlock sherlock;

    @Get
    public Object index() {
        return new Test("Coucou");
    }

    @Post("/head")
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<Mei> parseMeiHead(@Valid @Body FileUrl body) throws IOException {
        URL meiFileUrl = new URL(body.getFileUrl());
        BufferedReader in = new BufferedReader(new InputStreamReader(meiFileUrl.openStream()));

        Mei mei = JAXB.unmarshal(in, Mei.class);

//        Model model = ModelFactory.createDefaultModel();
//
//        Resource e21_composer = model.createResource(sherlock.makeIri());
//
//        Resource f1 = model.createResource(sherlock.makeIri())
//                .addProperty(RDF.type, LRMOO.F1_Work);
//        Resource f27 = model.createResource(sherlock.makeIri())
//                .addProperty(RDF.type, LRMOO.F27_Work_Creation);
//
//        Resource f2 = model.createResource(sherlock.makeIri())
//                .addProperty(RDF.type, LRMOO.F2_Expression)
//                .addProperty(CIDOCCRM.P2_has_type, Types.E55_MEI_CONTENT);
//        Resource f1 = model.createResource(sherlock.makeIri())
//                .addProperty(RDF.type, LRMOO.F1_Work)
//                .addProperty(LRMOO.R3_is_realised_in, f2);
//        Resource f3 = model.createResource(sherlock.makeIri())
//                .addProperty(RDF.type, LRMOO.F3_Manifestation)
//                .addProperty(CIDOCCRM.P2_has_type, Types.E55_XML_MEI_FILE)
//                .addProperty(LRMOO.R4_embodies, f2);

        // COM
        // AGN
        // voices
        // ENC
        // END
        // EED
        // EEV

//        for (Mei.MeiHead.ExtMeta.MetaFrame f : mei.meiHead.extMeta.frames) {
//            String k = f.frameInfo.referenceKey;
//            String v = f.frameInfo.referenceValue;
//
//            switch (k) {
//                case "CDT":
//                    Resource e13 = model.createResource(sherlock.makeIri())
//                            .addProperty(RDF.type, CIDOCCRM.E13_Attribute_Assignment);
//                    Resource e52 = model.createResource(sherlock.makeIri())
//                            .addProperty(RDF.type, CIDOCCRM.E52_Time_span)
//                            .addProperty(CIDOCCRM.P82_at_some_time_within, v);
//                    e13.addProperty(CIDOCCRM.P140_assigned_attribute_to, );
//                    e13.addProperty(CIDOCCRM.P177_assigned_property_of_type, );
//                    e13.addProperty(CIDOCCRM.P141_assigned, e52);
//            }
//        }

//        String json = sherlock.modelToJson(model);
        return HttpResponse.ok(mei);
    }
}
