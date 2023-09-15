package fr.cnrs.iremus.sherlock.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.e13.NewE13;
import fr.cnrs.iremus.sherlock.pojo.e13.NewP141;
import fr.cnrs.iremus.sherlock.service.E13Service;
import fr.cnrs.iremus.sherlock.service.ResourceService;
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
import jakarta.validation.Valid;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.util.List;

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

    @Inject
    ResourceService resourceService;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> create(@RequestBody(content = {@Content(mediaType = "application/json", schema = @Schema(implementation = NewE13.class), examples = {@ExampleObject(name = "Simple E13", value = """
            {
                "p140": "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence",
                "p177": "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                "p141": "Ce n'est pas une cadence.",
                "p141_type": "literal",
                "document_context": "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
            }
            """), @ExampleObject(name = "E13 and new resource as P141", value = """
                                    {
                            "p140": "http://data-iremus.huma-num/id/mon-fragment-d-estampe",
                            "p177": "crm:P1_is_identified_by",
                            "new_p141": {
                                "rdf_type": ["crm:E42_Identifier"],
                                "p2_type": ["http://data-iremus.huma-num/id/identifiant-iiif", "http://data-iremus.huma-num/id/element-visuel"]
                            },
                            "p141_type": "new resource",
                            "document_context": "http://data-iremus.huma-num/id/mon-e36-estampe",
                            "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
                        }
            """)})}) @Valid @Body NewE13 body, Authentication authentication) throws ParseException {
        // new e13
        String e13Iri = sherlock.makeIri();
        String p177 = sherlock.resolvePrefix(body.getP177());
        String p141 = sherlock.resolvePrefix(body.getP141_type() == ResourceType.NEW_RESOURCE ? sherlock.makeIri() : body.getP141());
        String documentContext = sherlock.resolvePrefix(body.getDocument_context());
        String analyticalProject = sherlock.resolvePrefix(body.getAnalytical_project());
        ResourceType p141Type = body.getP141_type();

        // UPDATE QUERY
        Model m = ModelFactory.createDefaultModel();
        List<Resource> p140s = body.getP140().stream().map(p140 -> m.createResource(sherlock.resolvePrefix(p140))).toList();
        Resource e13 = m.createResource(e13Iri);
        e13Service.insertNewE13(
                e13,
                p140s,
                p141Type.equals(ResourceType.LITERAL) ? m.createLiteral(p141) : m.createResource(p141),
                m.createResource(p177),
                m.createResource(documentContext),
                m.createResource(analyticalProject),
                m,
                authentication
        );

        if (body.getP141_type() == ResourceType.NEW_RESOURCE) {
            NewP141 newP141 = body.getNew_p141();
            Resource p141Resource = m.createResource(p141);

            resourceService.insertResourceCommonTriples(p141Resource, authentication, m);
            for (String rdfType : newP141.getRdf_type())
                m.add(p141Resource, RDF.type, m.createResource(sherlock.resolvePrefix(rdfType)));
            for (String p2Type : newP141.getP2_type()) {
                m.add(p141Resource, CIDOCCRM.P2_has_type, m.createResource(sherlock.resolvePrefix(p2Type)));
            }
            if (newP141.getP190() != null) {
                m.add(p141Resource, CIDOCCRM.P190_has_symbolic_content, sherlock.resolvePrefix(newP141.getP190()));
            }
        }

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);
            Model currentModel = e13Service.getModelByE13(e13);

            return HttpResponse.created(sherlock.modelToJson(currentModel));
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
            for (RDFNode p141 : p141List) {
                if (p141.isResource()) {
                    List<Resource> resourcesDependingOnP141 = currentModel.listSubjectsWithProperty(null, p141.asResource()).filterDrop(resource -> resource.equals(e13)).toList();
                    if (!resourcesDependingOnP141.isEmpty()) {
                        return HttpResponse.status(HttpStatus.FORBIDDEN).body(sherlock.objectToJson("Please delete entities which depends on the P141 of the E13 first."));
                    }

                    // If P141 does not belong to current user, do NOT consider it
                    if (!currentModel.contains(p141.asResource(), DCTerms.creator, authenticatedUser)) {
                        currentModel.removeAll(p141.asResource(), null, null);
                    }

                    // If no propagation, do NOT remove triples with 141 as subject
                    if (!Boolean.TRUE.equals(propagate)) {
                        currentModel.removeAll(p141.asResource(), null, null);
                    }
                }
            }

            List<RDFNode> involvedUsers = currentModel.listObjectsOfProperty(DCTerms.creator).toList();
            if (!involvedUsers.stream().allMatch(rdfNode -> authenticatedUser.toString().equals(rdfNode.toString()))) {
                return HttpResponse.status(HttpStatus.FORBIDDEN).body(sherlock.objectToJson("Some resources belongs to other users."));
            }

            conn.update(sherlock.makeDeleteQuery(currentModel));

            return HttpResponse.ok(sherlock.modelToJson(currentModel));
        }
    }
}
