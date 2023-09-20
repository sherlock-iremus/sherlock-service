package fr.cnrs.iremus.sherlock.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.e90.NewE90Fragment;
import fr.cnrs.iremus.sherlock.service.E90Service;
import fr.cnrs.iremus.sherlock.service.ResourceService;
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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
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

@Controller("/api/e90")
@Tag(name = "3. Annotations")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class E90Controller {
    public static final String E90_POST_FRAGMENT_NO_RDFTYPE = "Parent resource has no rdf:type matching E90.";
    public static final String E90_DELETE_FRAGMENT_DOES_NOT_EXIST = "This E90 does not exist.";
    public static final String E90_DELETE_FRAGMENT_PLEASE_DELETE_ENTITIES = "Please delete entities which depends on the this E90 before deleting it.";
    public static final String E90_DELETE_FRAGMENT_BELONGS_TO_ANOTHER_USER = "This E90 belongs to other users.";

    @Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    E90Service e90Service;

    @Inject
    ResourceService resourceService;

    @Post("/fragment")
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> create(@RequestBody(content = {@Content(mediaType = "application/json", schema = @Schema(implementation = NewE90Fragment.class), examples = {@ExampleObject(value = """
            {
                "parent": "http://data-iremus.huma-num.fr/id/24c7c452-e9cf-4280-bb23-a66744f74835",
                "p2_type": ["http://data-iremus.huma-num/id/identifiant-iiif", "http://data-iremus.huma-num/id/element-visuel"]
            }
            """)})}) @Valid @Body NewE90Fragment body, Authentication authentication) throws ParseException, JsonProcessingException {
        Model m = ModelFactory.createDefaultModel();

        Resource parentE90 = m.createResource(sherlock.resolvePrefix(body.getParent()));
        Resource e90Type;
        try {
            e90Type = e90Service.getMostAccurateE90RDFType(parentE90);
        } catch (Exception e) {
            return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + E90_POST_FRAGMENT_NO_RDFTYPE + "\"}");
        }

        Resource e90Fragment = m.createResource(sherlock.makeIri());
        resourceService.insertResourceCommonTriples(e90Fragment, authentication, m);
        m.add(e90Fragment, RDF.type, e90Type);
        m.add(e90Fragment, CIDOCCRM.P106i_forms_part_of, parentE90);
        for (String p2_type : body.getP2_type()) {
            m.add(e90Fragment, CIDOCCRM.P2_has_type, m.createResource(sherlock.resolvePrefix(p2_type)));
        }

        String updateWithModel = sherlock.makeUpdateQuery(m);

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            // WRITE
            conn.update(updateWithModel);

            // AND READ IT BACK AS JSON-LD
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e90Fragment, "?p", "?o")
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder().addWhere(e90Fragment, "?p", "?o")
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model res = qe.execConstruct();

            return HttpResponse.created(sherlock.modelToJson(res));
        }
    }

    @Delete("/fragment/{e90Uuid}")
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<String> delete(@PathVariable String e90Uuid, Authentication authentication) throws HttpException, JsonProcessingException {
        Model m = ModelFactory.createDefaultModel();
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Resource authenticatedUser = m.getResource(sherlock.makeIri(authenticatedUserUuid));
        Resource e90 = m.getResource(sherlock.makeIri(e90Uuid));

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e90, "?p", "?o")
                    .addConstruct("?s", "?s_p", e90)
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder()
                                    .addWhere(e90, "?p", "?o")
                                    .addOptional("?s", "?s_p", e90)
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model e90Model = qe.execConstruct();

            if (!e90Model.containsResource(e90))
                return HttpResponse.notFound("{\"message\":\"" + E90_DELETE_FRAGMENT_DOES_NOT_EXIST + "\"}");

            List<Resource> resourcesWithE90AsObject = e90Model.listSubjectsWithProperty(null, e90).toList();
            if (!resourcesWithE90AsObject.isEmpty())
                return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\":\"" + E90_DELETE_FRAGMENT_PLEASE_DELETE_ENTITIES + "\"}");

            List<RDFNode> involvedUsers = e90Model.listObjectsOfProperty(e90, DCTerms.creator).toList();
            if (!involvedUsers.stream().allMatch(rdfNode -> authenticatedUser.toString().equals(rdfNode.toString())))
                return HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\":\"" + E90_DELETE_FRAGMENT_BELONGS_TO_ANOTHER_USER + "\"}");

            conn.update(sherlock.makeDeleteQuery(e90Model));

            return HttpResponse.ok(sherlock.modelToJson(e90Model));
        }
    }
}