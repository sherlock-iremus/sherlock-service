package fr.cnrs.iremus.sherlock.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.e13.NewE13;
import fr.cnrs.iremus.sherlock.service.E13Service;
import io.micronaut.context.annotation.Property;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdf.model.impl.SelectorImpl;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.DCTerms;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

@Controller("/api/e13")
@Tag(name = "3. Annotations")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class E13Controller {
    @Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    E13Service e13Service;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> create(@RequestBody( content= { @Content( mediaType = "application/json", schema = @Schema(implementation = NewE13.class), examples = {@ExampleObject(value = """
                        {
                            "p140": "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence",
                            "p177": "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                            "p141": "Ce n'est pas une cadence.",
                            "p141_type": "literal",
                            "document_context": "http://data-iremus.huma-num/id/ma-partition",
                            "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
                        }
                        """)})}) @Valid @Body NewE13 body, Authentication authentication) throws ParseException {
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

            return HttpResponse.ok(sherlock.modelToJson(res));
        }
    }

    /**
     * @param propagate set to "true" if you want to delete also the 141 of the E13
     */
    @Delete("/{e13Uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> delete(@PathVariable String e13Uuid, @QueryValue @Nullable Boolean propagate, Authentication authentication) throws HttpException, JsonProcessingException {
        Model m = ModelFactory.createDefaultModel();
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Resource authenticatedUser = m.getResource(sherlock.makeIri(authenticatedUserUuid));
        Resource e13 = m.getResource(sherlock.makeIri(e13Uuid));

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            Model currentModel = e13Service.getModelByE13(e13);

            if (!currentModel.containsResource(e13))
                return HttpResponse.notFound(sherlock.objectToJson("This E13 does not exist."));

            List<RDFNode> p141List = currentModel.listObjectsOfProperty(e13, CIDOCCRM.P141_assigned).toList();
            for(RDFNode p141 : p141List) {
                if (p141.isResource()) {
                    List<Resource> resourcesDependingOnP141 = currentModel.listSubjectsWithProperty(null, p141.asResource()).filterDrop(resource -> resource.equals(e13)).toList();
                    if (! resourcesDependingOnP141.isEmpty()) {
                        return HttpResponse.status(HttpStatus.FORBIDDEN).body(sherlock.objectToJson("Please delete entities which depends on the P141 of the E13 first."));
                    }

                    // If P141 does not belong to current user, do NOT consider it
                    if (! currentModel.contains(p141.asResource(), DCTerms.creator, authenticatedUser)) {
                        currentModel.removeAll(p141.asResource(), null, null);
                    }

                    // If no propagation, do NOT remove triples with 141 as subject
                    if (! Boolean.TRUE.equals(propagate)) {
                        currentModel.removeAll(p141.asResource(), null, null);
                    }
                }
            }

            List<RDFNode> involvedUsers = currentModel.listObjectsOfProperty(DCTerms.creator).toList();
            if (! involvedUsers.stream().allMatch(rdfNode -> authenticatedUser.toString().equals(rdfNode.toString()))) {
                return HttpResponse.status(HttpStatus.FORBIDDEN).body(sherlock.objectToJson("Some resources belongs to other users."));
            }

            conn.update(sherlock.makeDeleteQuery(currentModel));

            return HttpResponse.ok(sherlock.modelToJson(currentModel));
        }
    }
}
