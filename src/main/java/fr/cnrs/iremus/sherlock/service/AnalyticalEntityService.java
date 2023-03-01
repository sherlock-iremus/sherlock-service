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
                    .addConstruct("?e13_creation", "?e13_creation_p", "?e13_creation_o")
                    .addConstruct("?analytical_project", CIDOCCRM.P9_consists_of, "?e13_creation")

                    .addConstruct("?e13_annotation", "?e13_annotation_p", "?e13_annotation_o")
                    .addConstruct("?analytical_project_annotation", CIDOCCRM.P9_consists_of, "?e13_annotation")
                    .addGraph(sherlock.getGraph(),
                            new WhereBuilder()
                                    .addWhere(analyticalEntity, "?analyticalEntity_p", "?analyticalEntity_o")
                                    .addWhere("?e13_creation", CIDOCCRM.P141_assigned, analyticalEntity)
                                    .addWhere("?e13_creation", "?e13_creation_p", "?e13_creation_o")
                                    .addWhere("?analytical_project", CIDOCCRM.P9_consists_of, "?e13_creation")

                                    .addWhere("?e13_annotation", CIDOCCRM.P140_assigned_attribute_to, analyticalEntity)
                                    .addWhere("?e13_annotation", "?e13_annotation_p", "?e13_annotation_o")
                                    .addWhere("?analytical_project_annotation", CIDOCCRM.P9_consists_of, "?e13_annotation")
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }
}