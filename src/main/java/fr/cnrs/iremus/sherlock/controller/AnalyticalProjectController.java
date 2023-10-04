package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.analyticalProject.NewAnalyticalProject;
import fr.cnrs.iremus.sherlock.service.DateService;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
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

@Controller("/api/analytical-project")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "4. Structural")
public class AnalyticalProjectController {
    private static Logger logger = LoggerFactory.getLogger(AnalyticalProjectController.class);
    public final static String e55analyticalProjectIri = "http://data-iremus.huma-num.fr/id/21816195-6708-4bbd-a758-ee354bb84900";
    public final static String e55draftIri = "http://data-iremus.huma-num.fr/id/cabe46bf-23d4-4392-aa20-b3eb21ad7dfd";
    @Property(name = "jena")
    protected String jena;
    @Inject
    Sherlock sherlock;
    @Inject
    DateService dateService;

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

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(analyticalProject, "?e7_p", "?e7_o")
                    .addConstruct("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                    .addGraph(sherlock.getGraph(), new WhereBuilder()
                            .addWhere(analyticalProject, "?e7_p", "?e7_o")
                            .addWhere(analyticalProject, "?e7_p_time_span", "?e7_o_time_span")
                            .addWhere("?e7_o_time_span", RDF.type, CIDOCCRM.E52_Time_span)
                            .addWhere("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return HttpResponse.created(sherlock.modelToJson(res));
        }

    }
}