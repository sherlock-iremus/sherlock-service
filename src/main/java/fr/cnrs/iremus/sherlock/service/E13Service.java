package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.security.authentication.Authentication;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.util.List;
import java.util.Objects;

import static fr.cnrs.iremus.sherlock.controller.E13Controller.*;

@Singleton
public class E13Service {
    @io.micronaut.context.annotation.Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    ResourceService resourceService;

    public void insertNewE13(Resource e13, List<Resource> p140s, RDFNode p141, Resource p177, Resource documentContext, Resource analyticalProject, Model m, Authentication authentication, String date) {
        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        insertNewE13(e13, p140s, p141, p177, documentContext, analyticalProject, m, authenticatedUser, date);
    }

    public void insertNewE13(Resource e13, List<Resource> p140s, RDFNode p141, Resource p177, Resource documentContext, Resource analyticalProject, Model m, Resource user, String date) {
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
        resourceService.insertResourceCommonTriples(e13, user, m, date);
    }
    public Model getModelByE13(Resource e13) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e13, "?e13_p", "?e13_o")
                    .addConstruct("?e13_s", "?e13_p_i", e13)
                    .addConstruct("?analytical_project", CIDOCCRM.P9_consists_of, e13)
                    .addConstruct("?p141", "?p141_p", "?p141_o")
                    .addConstruct("?p141_s", "?p141_p_i", "?p141")
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder()
                                    .addWhere(e13, "?e13_p", "?e13_o")
                                    .addWhere("?e13_s", "?e13_p_i", e13)
                                    .addWhere("?analytical_project", CIDOCCRM.P9_consists_of, e13)
                                    .addOptional(new WhereBuilder()
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

    public boolean hasE13IncomingTriples(Model currentModel, Resource e13) {
        return currentModel.listStatements(null, null, e13).toList().stream().anyMatch(triple -> !Objects.equals(triple.getPredicate().asNode().getURI(), CIDOCCRM.P9_consists_of.asNode().getURI()));
    }

    public boolean isE13Creator(Model currentModel, Resource e13, Resource authenticatedUser) {
        return currentModel.contains(e13, CIDOCCRM.P14_carried_out_by, authenticatedUser);
    }

    public boolean hasP141NoIncomingTriple(Model currentModel, Resource e13) {
        Resource p141 = currentModel.listObjectsOfProperty(e13, CIDOCCRM.P141_assigned).next().asResource();
        // We compare to "1" because there should be only the triple :  e13 -- P141 --> object.
        return currentModel.listStatements(null, null, p141).toList().size() == 1;
    }

    public Model getModelByE13WithoutP141Triples(Resource e13) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e13, "?e13_p", "?e13_o")
                    .addConstruct("?e13_s", "?e13_p_i", e13)
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder()
                                    .addWhere(e13, "?e13_p", "?e13_o")
                                    .addWhere("?e13_s", "?e13_p_i", e13)
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }

    public Model getModelByE13WithoutIncomingP141Triples(Resource e13) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(e13, "?e13_p", "?e13_o")
                    .addConstruct("?e13_s", "?e13_p_i", e13)
                    .addConstruct("?analytical_project", CIDOCCRM.P9_consists_of, e13)
                    .addConstruct("?p141", "?p141_p", "?p141_o")
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder()
                                    .addWhere(e13, "?e13_p", "?e13_o")
                                    .addWhere("?e13_s", "?e13_p_i", e13)
                                    .addWhere("?analytical_project", CIDOCCRM.P9_consists_of, e13)
                                    .addOptional(new WhereBuilder()
                                            .addWhere(e13, CIDOCCRM.P141_assigned, "?p141")
                                            .addOptional("?p141", "?p141_p", "?p141_o")
                                    )
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }

    public boolean isP141LinkedToE13(Model currentModel, Resource e13) {
        Resource p141 = currentModel.listObjectsOfProperty(e13, CIDOCCRM.P141_assigned).next().asResource();
        Literal e13CreationDate = currentModel.listObjectsOfProperty(e13, DCTerms.created).next().asLiteral();
        return currentModel.contains(p141, DCTerms.created, e13CreationDate);
    }

    public Model getDeletableModelForE13(Resource e13) throws SherlockServiceException {
        Model currentModel = getModelByE13(e13);

        if (!currentModel.containsResource(e13))
            throw new SherlockServiceException(HttpResponse.notFound("{\"message\": \"" + E13_DELETE_DOES_NOT_EXIST + "\"}"));

        if (!currentModel.contains(e13, RDF.type, CIDOCCRM.E13_Attribute_Assignment))
            throw new SherlockServiceException(HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + E13_DELETE_IS_NOT_E13 + "\"}"));

        // Linked means "have been created at the same time"
        if (isP141LinkedToE13(currentModel, e13)) {
            if (hasP141NoIncomingTriple(currentModel, e13)) {
                return currentModel;
            } else {
                throw new SherlockServiceException(HttpResponse.status(HttpStatus.FORBIDDEN).body("{\"message\": \"" + E13_DELETE_PLEASE_ENTITIES_FIRST + "\"}"));
            }
        } else {
            return getModelByE13WithoutP141Triples(e13);
        }
    }
}

