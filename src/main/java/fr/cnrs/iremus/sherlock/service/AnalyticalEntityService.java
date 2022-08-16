package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import jakarta.inject.Inject;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;

public class AnalyticalEntityService {
    @io.micronaut.context.annotation.Property(name = "jena")
    protected String jena;
    @Inject
    Sherlock sherlock;
    public Model getModelByAnalyticalEntity(Resource analyticalEntity) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(analyticalEntity, "?analyticalEntity_p", "?analyticalEntity_o")
                    .addConstruct(analyticalEntity, "?analyticalEntity_p", "?analyticalEntity_o")
                    .addConstruct("?analyticalEntity_e13", CIDOCCRM.P140_assigned_attribute_to, analyticalEntity)
                    .addConstruct("?analyticalEntity_e13", "?analyticalEntity_e13_p", "?analyticalEntity_e13_o")
                    .addConstruct("?e13", CIDOCCRM.P141_assigned, analyticalEntity)
                    .addConstruct("?e13", "?e13_p", "?e13_o")
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder()
                                    .addWhere(analyticalEntity, "?analyticalEntity_p", "?analyticalEntity_o")
                                    .addOptional(
                                            new WhereBuilder()
                                                    .addWhere("?analyticalEntity_e13", CIDOCCRM.P140_assigned_attribute_to, analyticalEntity)
                                                    .addWhere("?analyticalEntity_e13", "?analyticalEntity_e13_p", "?analyticalEntity_e13_o")
                                    )
                                    .addWhere("?e13", CIDOCCRM.P141_assigned, analyticalEntity)
                                    .addWhere("?e13", "?e13_p", "?e13_o")
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }
}