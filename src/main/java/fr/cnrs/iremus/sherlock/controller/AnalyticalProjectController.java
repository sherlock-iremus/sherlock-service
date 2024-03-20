package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.analyticalProject.NewAnalyticalProject;
import fr.cnrs.iremus.sherlock.service.AnalyticalProjectService;
import fr.cnrs.iremus.sherlock.service.DateService;
import fr.cnrs.iremus.sherlock.service.E13Service;
import fr.cnrs.iremus.sherlock.service.SherlockServiceException;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller("/api/analytical-project")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "4. Structural")
public class AnalyticalProjectController {
    private static Logger logger = LoggerFactory.getLogger(AnalyticalProjectController.class);
    public final static String e55analyticalProjectIri = "http://data-iremus.huma-num.fr/id/21816195-6708-4bbd-a758-ee354bb84900";
    public final static String e55draftIri = "http://data-iremus.huma-num.fr/id/cabe46bf-23d4-4392-aa20-b3eb21ad7dfd";
    public final static String ANALYTICAL_PROJECT_BELONGS_TO_ANOTHER_USER = "This analytical project belongs to somebody else. Ask them do to deletion themself";
    public final static String RESOURCE_IS_NOT_AN_ANALYTICAL_PROJECT = "This resource is not an analytical project";
    @Property(name = "jena")
    protected String jena;
    @Inject
    Sherlock sherlock;
    @Inject
    DateService dateService;
    @Inject
    AnalyticalProjectService analyticalProjectService;
    @Inject
    E13Service e13Service;

    @ApiResponse(responseCode = "200", description = "new analytical entity's model")
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> create(@RequestBody(content = {@Content(mediaType = "application/json", schema = @Schema(implementation = NewAnalyticalProject.class), examples = {@ExampleObject(value = """
            {
                "label": "mon projet analytique"
            }
            """)})}) @Valid @Body NewAnalyticalProject body, Authentication authentication) throws ParseException {
        logger.info("Creating analytical project with name '%s' for user %s".formatted(body.getLabel(), authentication.getAttributes().get("uuid")));

        // context

        String now = dateService.getNow();
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");

        // resources

        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        Resource analyticalProject = m.createResource(sherlock.makeIri());
        Resource timeSpan = m.createResource(sherlock.makeIri());

        // triples

        m.add(analyticalProject, RDF.type, CIDOCCRM.E7_Activity);
        m.add(analyticalProject, DCTerms.creator, authenticatedUser);
        m.add(analyticalProject, CIDOCCRM.P2_has_type, m.createResource(e55analyticalProjectIri));
        m.add(analyticalProject, CIDOCCRM.P14_carried_out_by, authenticatedUser);
        m.add(analyticalProject, CIDOCCRM.P1_is_identified_by, body.getLabel());
        m.add(analyticalProject, CIDOCCRM.P4_has_time_span, timeSpan);
        m.add(analyticalProject, Sherlock.has_privacy_type, m.createResource(e55draftIri));
        m.add(timeSpan, RDF.type, CIDOCCRM.E52_Time_span);
        m.add(timeSpan, CIDOCCRM.P82a_begin_of_the_begin, now);

        // update dataset

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            conn.update(updateWithModel);
        }

        // query and return updated dataset

        return HttpResponse.created(sherlock.modelToJson(analyticalProjectService.getAnalyticalProject(analyticalProject)));
    }

    @ApiResponse(responseCode = "200", description = "model containing every triple deleted")
    @Delete("/{analyticalProjectUuid}") // 572423c3-5019-47be-b845-6b96fbddc754
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> delete(@PathVariable String analyticalProjectUuid, Authentication authentication) throws ParseException {
        logger.info("Trying to delete analytical project : {}", analyticalProjectUuid);

        // context

        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");

        // resources

        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        Resource analyticalProject = m.createResource(sherlock.makeIri(analyticalProjectUuid));

        // get analytical project and verify deletable

        Model analyticalProjectModel = analyticalProjectService.getAnalyticalProject(analyticalProject);

        if (!analyticalProjectModel.contains(analyticalProject, CIDOCCRM.P2_has_type, m.createResource(e55analyticalProjectIri)))
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + RESOURCE_IS_NOT_AN_ANALYTICAL_PROJECT + "\"}");

        if (!analyticalProjectModel.contains(analyticalProject, CIDOCCRM.P14_carried_out_by, authenticatedUser))
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + ANALYTICAL_PROJECT_BELONGS_TO_ANOTHER_USER + "\"}");


        // todo: do transactional
        // delete all project's E13
        analyticalProjectModel.listObjectsOfProperty(analyticalProject, CIDOCCRM.P9_consists_of).
                forEach(e13 -> {
                    logger.info("Deleting analytical project's e13 {}", ((Resource) e13).getURI());

                    Model deletableModelForE13;
                    try {
                        deletableModelForE13 = e13Service.getDeletableModelForE13(e13.asResource());
                    } catch (SherlockServiceException exception) {
                        logger.warn("Could not delete e13 : {}", exception.getMessage());
                        return;
                    }

                    RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
                    try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
                        conn.update(sherlock.makeDeleteQuery(deletableModelForE13));
                    }
                });
        // Reading again analytical project's metadata, because P9s...
        analyticalProjectModel = analyticalProjectService.getAnalyticalProjectAndIncomingTriples(analyticalProject);

        // delete project and return it

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            logger.info("Deleting analytical project main data");
            conn.update(sherlock.makeDeleteQuery(analyticalProjectModel));
            return HttpResponse.ok(sherlock.modelToJson(analyticalProjectModel));
        }

    }
}