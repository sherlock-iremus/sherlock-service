package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.analyticalEntity.NewAnalyticalEntity;
import fr.cnrs.iremus.sherlock.pojo.e13.E13AsLinkToP141;
import fr.cnrs.iremus.sherlock.service.AnalyticalEntityService;
import fr.cnrs.iremus.sherlock.service.DateService;
import fr.cnrs.iremus.sherlock.service.E13Service;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
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

import javax.validation.Valid;

@Controller("/api/analytical-entity")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "5. Analytical Entity")
public class AnalyticalEntityController {
    @Inject
    Sherlock sherlock;

    @Inject
    DateService dateService;

    @Inject
    E13Service e13Service;

    @Inject
    AnalyticalEntityService analyticalEntityService;

    @Property(name = "jena")
    protected String jena;

    public final static String e55analyticalEntityIri = "http://data-iremus.huma-num.fr/id/6d72746a-9f28-4739-8786-c6415d53c56d";

    @ApiResponse(responseCode = "200", description = "uri of the analytical entity")
    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> create(@Valid @Body NewAnalyticalEntity body, Authentication authentication) throws ParseException {
        Model m = ModelFactory.createDefaultModel();
        String now = dateService.getNow();
        Resource authenticatedUser = m.createResource(sherlock.makeIri((String) authentication.getAttributes().get("uuid")));


        //region create E28 AnalyticalEntity

        Resource e28 = m.createResource(sherlock.makeIri());
        m.add(e28, DCTerms.created, now);
        m.add(e28, RDF.type, CIDOCCRM.E28_Conceptual_Object);
        m.add(e28, CIDOCCRM.P2_has_type, m.createResource(e55analyticalEntityIri));
        m.add(e28, DCTerms.creator, authenticatedUser);

        //endregion

        //region link E28 to its subject

        Resource e13AnalyticalEntityCreation = m.createResource(sherlock.makeIri());
        m.add(e13AnalyticalEntityCreation, CIDOCCRM.P141_assigned, e28);
        m.add(e13AnalyticalEntityCreation, RDF.type, CIDOCCRM.E13_Attribute_Assignment);
        m.add(e13AnalyticalEntityCreation, CIDOCCRM.P177_assigned_property_of_type, m.createResource(body.getP177()));
        m.add(e13AnalyticalEntityCreation, CIDOCCRM.P140_assigned_attribute_to, m.createResource(body.getP140()));
        m.add(e13AnalyticalEntityCreation, CIDOCCRM.P14_carried_out_by, authenticatedUser);
        m.add(e13AnalyticalEntityCreation, DCTerms.created, now);

        //endregion

        //region link all analysis to e28
        if (body.getE13s() != null) {
            for (E13AsLinkToP141 e13AsLinkToP141: body.getE13s()) {
                if (e13AsLinkToP141.getP141_type() == ResourceType.LITERAL) {
                    e13Service.insertNewE13(null, e28, m.createLiteral(e13AsLinkToP141.getP141()), m.createResource(e13AsLinkToP141.getP177()), m, authenticatedUser);
                } else {
                    e13Service.insertNewE13(null, e28, m.createResource(e13AsLinkToP141.getP141()), m.createResource(e13AsLinkToP141.getP177()), m, authenticatedUser);
                }
            }
        }
        //endregion

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e28, "?p", "?o")
                    .addGraph(sherlock.getGraph(), e28, "?p", "?o");
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return HttpResponse.ok(sherlock.modelToJson(res));
        }
    }

    @ApiResponse(responseCode = "200", description = "model deleted")
    @Delete("/{analyticalEntityUuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> delete(@PathVariable String analyticalEntityUuid, Authentication authentication) throws HttpException {
        Model m = ModelFactory.createDefaultModel();
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Resource authenticatedUser = m.getResource(sherlock.makeIri(authenticatedUserUuid));
        Resource analyticalEntity = m.getResource(sherlock.makeIri(analyticalEntityUuid));

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Model currentModel = analyticalEntityService.getModelByAnalyticalEntity(analyticalEntity);
            if (!currentModel.containsResource(analyticalEntity))
                return HttpResponse.notFound("This analytical entity does not exist.");
            if (!currentModel.contains(analyticalEntity, DCTerms.creator, authenticatedUser))
                return HttpResponse.unauthorized();

            conn.update(sherlock.makeDeleteQuery(currentModel));

            return HttpResponse.ok(sherlock.modelToJson(currentModel));
        }

    }
}