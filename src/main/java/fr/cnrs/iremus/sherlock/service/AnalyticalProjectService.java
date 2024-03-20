package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.WhereBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.RDF;

@Singleton
public class AnalyticalProjectService {
    @Property(name = "jena")
    protected String jena;
    @Inject
    Sherlock sherlock;

    public Model getAnalyticalProject(Resource analyticalProject) {

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(analyticalProject, "?e7_p", "?e7_o")
                    .addConstruct("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                    .addGraph(sherlock.getGraph(), new WhereBuilder()
                            .addWhere(analyticalProject, "?e7_p", "?e7_o")
                            .addWhere(analyticalProject, "?e7_p_time_span", "?e7_o_time_span")
                            .addWhere("?e7_o_time_span", RDF.type, CIDOCCRM.E52_Time_span)
                            .addWhere("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }

    public Model getAnalyticalProjectAndIncomingTriples(Resource analyticalProject) {

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(analyticalProject, "?e7_p", "?e7_o")
                    .addConstruct("?e7_s", "?e7_p_i", analyticalProject)
                    .addConstruct("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                    .addGraph(sherlock.getGraph(), new WhereBuilder()
                            .addWhere(analyticalProject, "?e7_p", "?e7_o")
                            .addWhere(analyticalProject, "?e7_p_time_span", "?e7_o_time_span")
                            .addWhere("?e7_o_time_span", RDF.type, CIDOCCRM.E52_Time_span)
                            .addWhere("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                            .addOptional("?e7_s", "?e7_p_i", analyticalProject)
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }
}
