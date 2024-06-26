package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.analyticalProject.NewAnalyticalProject;
import fr.cnrs.iremus.sherlock.pojo.analyticalProject.UpdateAnalyticalProject;
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
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static fr.cnrs.iremus.sherlock.controller.E13Controller.E13_DELETE_PLEASE_ENTITIES_FIRST;

@Controller("/api/analytical-project")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "4. Structural")
public class AnalyticalProjectController {
    private static Logger logger = LoggerFactory.getLogger(AnalyticalProjectController.class);
    public final static String e55analyticalProjectIri = "http://data-iremus.huma-num.fr/id/21816195-6708-4bbd-a758-ee354bb84900";
    public final static String e55draftIri = "http://data-iremus.huma-num.fr/id/cabe46bf-23d4-4392-aa20-b3eb21ad7dfd";
    public final static String e55publishedIri = "http://data-iremus.huma-num.fr/id/54a5cf00-a46a-4435-b893-6eda0cdc5462";
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
                "label": "mon projet analytique",
                "contribution_graph": "tonalities-contributions"
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
        Resource contributionGraph = sherlock.makeGraph(body.getContribution_graph());

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

        String updateWithModel = sherlock.makeUpdateQuery(m, contributionGraph);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            conn.update(updateWithModel);
        }

        // query and return updated dataset

        return HttpResponse.created(sherlock.modelToJson(analyticalProjectService.getAnalyticalProject(analyticalProject)));
    }

    @ApiResponse(responseCode = "200", description = "updated analytical entity's model")
    @Patch("/{analyticalProjectUuid}") // 572423c3-5019-47be-b845-6b96fbddc754
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> update(@RequestBody(content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UpdateAnalyticalProject.class), examples = {@ExampleObject(value = """
            {
                "description": "la description de mon projet analytique",
                "contribution_graph": "tonalities-contributions"
            }
            """)})}) @Valid @Body UpdateAnalyticalProject body, @PathVariable String analyticalProjectUuid, Authentication authentication) {
        logger.info("Updating analytical project with uuid '%s' for user %s".formatted(analyticalProjectUuid, authentication.getAttributes().get("uuid")));
        // context

        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");

        // resources

        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        Resource analyticalProject = m.createResource(sherlock.makeIri(analyticalProjectUuid));

        // tests on data integrity

        Model analyticalProjectModel = analyticalProjectService.getAnalyticalProject(analyticalProject);

        if (!analyticalProjectModel.contains(analyticalProject, CIDOCCRM.P2_has_type, m.createResource(e55analyticalProjectIri)))
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + RESOURCE_IS_NOT_AN_ANALYTICAL_PROJECT + "\"}");

        if (!analyticalProjectModel.contains(analyticalProject, CIDOCCRM.P14_carried_out_by, authenticatedUser))
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + ANALYTICAL_PROJECT_BELONGS_TO_ANOTHER_USER + "\"}");

        return HttpResponse.ok(sherlock.modelToJson(
                analyticalProjectService.updateAnalyticalProject(
                        body.getLabel(),
                        body.getDescription(),
                        body.getColor(),
                        body.getPrivacyTypeUuid(),
                        body.getContribution_graph(),
                        analyticalProjectModel,
                        analyticalProject
                )));
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


        try (RDFConnection conn = RDFConnectionFuseki.connect(jena)) {
            conn.executeWrite(() -> {
                analyticalProjectModel.listObjectsOfProperty(analyticalProject, CIDOCCRM.P9_consists_of).
                        forEach(e13 -> {
                            logger.info("Deleting analytical project's e13 {}", ((Resource) e13).getURI());

                            Model deletableModelForE13;
                            try {
                                deletableModelForE13 = e13Service.getDeletableModelForE13(e13.asResource());
                            } catch (SherlockServiceException exception) {
                                // Bypass the generic e13 deletion exception because we are doing a batch delete on analytical project
                                if (! exception.getReason().equals(E13_DELETE_PLEASE_ENTITIES_FIRST)) {
                                    logger.warn("Could not delete e13 : {}", exception.getHttpResponse().body());
                                    return;
                                } else {
                                    deletableModelForE13 = exception.getModel();

                                    // Remove triples with P141 as object not to delete it.
                                    RDFNode p141 = deletableModelForE13.listObjectsOfProperty(CIDOCCRM.P141_assigned).next();
                                    for (Statement statement: deletableModelForE13.listStatements(null, null, p141).toList()) {
                                        if (!(statement.getSubject().equals(e13))) {
                                            deletableModelForE13.remove(statement);
                                        }
                                    }
                                }
                            }

                            conn.update(sherlock.makeDeleteQuery(deletableModelForE13));

                        });

                logger.info("Deleting analytical project main data");
                // Reading again analytical project's metadata, because P9s...
                conn.update(sherlock.makeDeleteQuery(analyticalProjectService.getAnalyticalProjectAndIncomingTriples(analyticalProject)));
            });
        }
        return HttpResponse.ok(sherlock.modelToJson(analyticalProjectModel));
    }
}