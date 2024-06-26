package fr.cnrs.iremus.sherlock.controller;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.ResourceType;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.e13.NewE13;
import fr.cnrs.iremus.sherlock.pojo.e13.NewP141;
import fr.cnrs.iremus.sherlock.service.DateService;
import fr.cnrs.iremus.sherlock.service.E13Service;
import fr.cnrs.iremus.sherlock.service.ResourceService;
import fr.cnrs.iremus.sherlock.service.SherlockServiceException;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.http.exceptions.HttpException;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.ExternalDocumentation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@Controller("/api/e13")
@Tag(name = "3. Annotations")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class E13Controller {
    @Property(name = "graphs.contributions")
    private List<String> contributionGraphs;
    private static Logger logger = LoggerFactory.getLogger(E13Controller.class);
    public static final String E13_DELETE_PLEASE_ENTITIES_FIRST = "Please delete entities which depends on the P141 first.";
    public static final String E13_DELETE_DOES_NOT_EXIST = "This E13 does not exist.";
    public static final String E13_DELETE_INCOMING_TRIPLES = "This E13 has incoming triples. Delete them.";
    public static final String E13_DELETE_BELONGS_TO_ANOTHER_USER = "This E13 belongs to anybody else.";
    public static final String E13_DELETE_IS_NOT_E13 = "This resource is not an E13";

    @Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    E13Service e13Service;

    @Inject
    DateService dateService;

    @Inject
    ResourceService resourceService;

    @Post
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> create(
            @RequestBody(content = {@Content(mediaType = "application/json", schema = @Schema(implementation = NewE13.class), examples = {@ExampleObject(name = "Simple E13", value = """
                    {
                        "p140": ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                        "p177": "http://data-iremus.huma-num.fr/id/commentaire-sur-entite-analytique",
                        "p141": "Ce n'est pas une cadence.",
                        "p141_type": "LITERAL",
                        "contribution_graph": "tonalities-contributions",
                        "document_context": "http://data-iremus.huma-num.fr/id/ma-partition",
                        "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
                    }
                    """), @ExampleObject(name = "E13 and new resource as P141", value = """
                                            {
                                    "p140": ["http://data-iremus.huma-num.fr/id/mon-fragment-d-estampe"],
                                    "p177": "crm:P1_is_identified_by",
                                    "contribution_graph": "tonalities-contributions",
                                    "new_p141": {
                                        "rdf_type": ["crm:E42_Identifier"],
                                        "p2_type": ["http://data-iremus.huma-num.fr/id/identifiant-iiif", "http://data-iremus.huma-num.fr/id/element-visuel"]
                                    },
                                    "p141_type": "NEW_RESOURCE",
                                    "document_context": "http://data-iremus.huma-num.fr/id/mon-e36-estampe",
                                    "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
                                }
                    """)})}) @Valid @Body NewE13 body,
            Authentication authentication
    ) throws ParseException {
        logger.info("Creating new E13 for user %s".formatted(authentication.getAttributes().get("uuid")));

        // new e13
        String e13Iri = sherlock.makeIri();
        String p177 = sherlock.resolvePrefix(body.getP177());
        String p141 = sherlock.resolvePrefix(body.getP141_type() == ResourceType.NEW_RESOURCE ? sherlock.makeIri() : body.getP141());
        String documentContext = sherlock.resolvePrefix(body.getDocument_context());
        String analyticalProject = sherlock.resolvePrefix(body.getAnalytical_project());
        String now = dateService.getNow();
        Resource contributionGraph = sherlock.makeGraph(body.getContribution_graph());
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
                authentication,
                now
        );

        if (body.getP141_type() == ResourceType.NEW_RESOURCE) {
            NewP141 newP141 = body.getNew_p141();
            Resource p141Resource = m.createResource(p141);
            resourceService.insertResourceCommonTriples(p141Resource, authentication, m, now);
            for (String rdfType : newP141.getRdf_type())
                m.add(p141Resource, RDF.type, m.createResource(sherlock.resolvePrefix(rdfType)));
            for (String p2Type : newP141.getP2_type()) {
                m.add(p141Resource, CIDOCCRM.P2_has_type, m.createResource(sherlock.resolvePrefix(p2Type)));
            }
            if (newP141.getP190() != null) {
                m.add(p141Resource, CIDOCCRM.P190_has_symbolic_content, sherlock.resolvePrefix(newP141.getP190()));
            }
        }

        String updateWithModel = sherlock.makeUpdateQuery(m, contributionGraph);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            conn.update(updateWithModel);
            Model currentModel = e13Service.getModelByE13WithoutIncomingP141Triples(e13);

            return HttpResponse.created(sherlock.modelToJson(currentModel));
        }
    }

    @Delete("/{e13Uuid}") // 572423c3-5019-47be-b845-6b96fbddc754
    @Produces(MediaType.APPLICATION_JSON)
    @ExternalDocumentation(description = "Workflow Miro", url = "https://miro.com/app/board/uXjVO1vwG0U=/?moveToWidget=3458764570720281878&cot=14")
    public MutableHttpResponse<String> delete(@PathVariable String e13Uuid, Authentication authentication) throws HttpException {
        logger.info("E13 %s deletion triggered by user : %s".formatted(e13Uuid, authentication.getAttributes().get("uuid")));

        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.getResource(sherlock.makeIri(authenticatedUserUuid));
        Resource e13 = m.getResource(sherlock.makeIri(e13Uuid));

        Model modelToDelete;
        try {
            modelToDelete = e13Service.getDeletableModelForE13(e13);
        } catch (SherlockServiceException e) {
            return e.getHttpResponse();
        }

        if (e13Service.hasE13IncomingTriples(modelToDelete, e13))
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + E13_DELETE_INCOMING_TRIPLES + "\"}");

        if (! e13Service.isE13Creator(modelToDelete, e13, authenticatedUser))
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + E13_DELETE_BELONGS_TO_ANOTHER_USER + "\"}");


        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            conn.update(sherlock.makeDeleteQuery(modelToDelete));

            return HttpResponse.ok(sherlock.modelToJson(modelToDelete));
        }
    }
}
