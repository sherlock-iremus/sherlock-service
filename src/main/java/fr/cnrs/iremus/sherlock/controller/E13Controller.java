package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.e13.NewE13;
import fr.cnrs.iremus.sherlock.service.DateService;
import fr.cnrs.iremus.sherlock.service.E13Service;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
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

@Controller("/api/e13")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class E13Controller {
    @Property(name = "jena")
    protected String jena;

    @Inject
    DateService dateService;

    @Inject
    Sherlock sherlock;

    @Inject
    E13Service e13Service;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public String create(@Valid @Body NewE13 body, Authentication authentication) throws ParseException {
        // context
        String now = dateService.getNow();
        // new e13
        String e13Iri = sherlock.makeIri();
        String p140 = sherlock.resolvePrefix(body.getP140());
        String p177 = sherlock.resolvePrefix(body.getP177());
        String p141 = sherlock.resolvePrefix(body.getP141());
        String documentContext = sherlock.resolvePrefix(body.getDocument_context());
        String analyticalProject = sherlock.resolvePrefix(body.getAnalytical_project());
        ResourceType p141Type = body.getP141_type();

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();
        Resource e13 = m.createResource(e13Iri);
        e13Service.insertNewE13(
                e13,
                m.createResource(p140),
                p141Type.equals(ResourceType.URI) ? m.createResource(p141) : m.createLiteral(p141),
                m.createResource(p177),
                m.createResource(documentContext),
                m.createResource(analyticalProject),
                m,
                authentication
        );

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e13, "?e13_p", "?e13_o")
                    .addGraph(sherlock.getGraph(), e13, "?e13_p", "?e13_o");
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return sherlock.modelToJson(res);
        }
    }
}
