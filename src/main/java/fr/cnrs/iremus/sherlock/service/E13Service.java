package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.RDF;

import java.util.List;

@Singleton
public class E13Service {
    @io.micronaut.context.annotation.Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    ResourceService resourceService;

    public void insertNewE13(Resource e13, List<Resource> p140s, RDFNode p141, Resource p177, Resource documentContext, Resource analyticalProject, Model m, Authentication authentication) {
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        insertNewE13(e13, p140s, p141, p177, documentContext, analyticalProject, m, authenticatedUser);

    }

    public void insertNewE13(Resource e13, List<Resource> p140s, RDFNode p141, Resource p177, Resource documentContext, Resource analyticalProject, Model m, Resource user) {
        if (e13 == null) {
            e13 = m.createResource(sherlock.makeIri());
        }
        m.add(e13, RDF.type, CIDOCCRM.E13_Attribute_Assignment);
        m.add(e13, CIDOCCRM.P14_carried_out_by, user);
        for (Resource p140 : p140s) {
            m.add(e13, CIDOCCRM.P140_assigned_attribute_to, p140);
        }
        m.add(e13, CIDOCCRM.P141_assigned, p141);
        m.add(e13, CIDOCCRM.P177_assigned_property_of_type, p177);
        m.add(e13, Sherlock.has_document_context, documentContext);
        m.add(analyticalProject, CIDOCCRM.P9_consists_of, e13);
        resourceService.insertResourceCommonTriples(e13, user, m);
    }

    public Model getModelByE13(Resource e13) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e13, "?e13_p", "?e13_o")
                    .addConstruct("?analytical_project", CIDOCCRM.P9_consists_of, e13)
                    .addConstruct("?p141", "?p141_p", "?p141_o")
                    .addConstruct("?p141_s", "?p141_p_i", "?p141")
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder()
                                    .addWhere(e13, "?e13_p", "?e13_o")
                                    .addWhere("?analytical_project", CIDOCCRM.P9_consists_of, e13)
                                    .addOptional( new WhereBuilder()
                                            .addWhere(e13, CIDOCCRM.P141_assigned, "?p141")
                                            .addOptional("?p141", "?p141_p", "?p141_o")
                                            .addOptional("?p141_s", "?p141_p_i", "?p141")
                                    )
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }

}
