package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.util.ArrayList;
import java.util.List;

@Singleton
public class ResourceService {
    @io.micronaut.context.annotation.Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    DateService dateService;

    public void insertResourceCommonTriples(Resource resource, Authentication authentication, Model m) {
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        insertResourceCommonTriples(resource, authenticatedUser, m);

    }

    public void insertResourceCommonTriples(Resource resource, Resource user, Model m) {
        if (resource == null) {
            resource = m.createResource(sherlock.makeIri());
        }
        m.add(resource, DCTerms.created, dateService.getNow());
        m.add(resource, DCTerms.creator, user);
    }


    public List<Resource> getResourceRDFTypes(Resource resource) {
        List<Resource> types = new ArrayList<>();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            SelectBuilder sb = new SelectBuilder()
                    .addVar("?type")
                    .addGraph(sherlock.getGraph(), new WhereBuilder().addWhere(
                            resource, RDF.type, "?type"
                    ));
            Query q = sb.build();
            ResultSet resultSet = conn.query(q).execSelect();
            while (resultSet.hasNext()) {
                QuerySolution qs = resultSet.next();
                types.add(qs.getResource("type"));
            }
            return types;
        }
    }

}
