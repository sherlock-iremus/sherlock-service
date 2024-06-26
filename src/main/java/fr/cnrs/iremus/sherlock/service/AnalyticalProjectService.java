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
import org.apache.jena.rdf.model.*;
import org.apache.jena.rdfconnection.RDFConnection;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.vocabulary.RDF;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static fr.cnrs.iremus.sherlock.service.UserService.e55HexColorUuid;

@Singleton
public class AnalyticalProjectService {
    @Property(name = "jena")
    protected String jena;
    @Inject
    Sherlock sherlock;
    private static Logger logger = LoggerFactory.getLogger(AnalyticalProjectService.class);

    public Model getAnalyticalProject(Resource analyticalProject) {

        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct(analyticalProject, "?e7_p", "?e7_o")
                    .addConstruct("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                    .addGraph("?g", new WhereBuilder()
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
                    .addConstruct("?e41", "?e41_p", "?e41_o")
                    .addGraph("?g", new WhereBuilder()
                            .addWhere(analyticalProject, "?e7_p", "?e7_o")
                            .addWhere(analyticalProject, "?e7_p_time_span", "?e7_o_time_span")
                            .addWhere("?e7_o_time_span", RDF.type, CIDOCCRM.E52_Time_span)
                            .addWhere("?e7_o_time_span", "?e7_o_p", "?e7_o_o")
                            .addOptional(new WhereBuilder()
                                    .addWhere(analyticalProject, CIDOCCRM.P1_is_identified_by, "?e41")
                                    .addWhere("?e41", RDF.type, CIDOCCRM.E41_Appellation)
                                    .addWhere("?e41", "?e41_p", "?e41_o"))
                            .addOptional("?e7_s", "?e7_p_i", analyticalProject)
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }
    }

    private Model getAnalyticalProjectColorE41P190(Resource analyticalProject) {
        Model m = ModelFactory.createDefaultModel();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct("?e41", CIDOCCRM.P190_has_symbolic_content, "?color")
                    .addGraph("?g", new WhereBuilder()
                            .addWhere(analyticalProject, CIDOCCRM.P1_is_identified_by, "?e41")
                            .addWhere("?e41", CIDOCCRM.P2_has_type, m.createResource(sherlock.makeIri(e55HexColorUuid)))
                            .addWhere("?e41", CIDOCCRM.P190_has_symbolic_content, "?color")
                    );
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            return qe.execConstruct();
        }

    }

    public Model updateAnalyticalProject(String label, String description, String color, String privacyTypeUuid, String contributionGraph, Model analyticalProjectModel, Resource analyticalProject) {
        Model modelToAdd = ModelFactory.createDefaultModel();
        try (RDFConnection conn = RDFConnectionFuseki.connect(jena)) {
            conn.executeWrite(() -> {
                Model modelToDelete = ModelFactory.createDefaultModel();

                if (label != null) {
                    List<RDFNode> titles = analyticalProjectModel.listObjectsOfProperty(analyticalProject, CIDOCCRM.P1_is_identified_by).filterKeep(RDFNode::isLiteral).toList();
                    if (!titles.isEmpty()) {
                        titles.forEach(title -> modelToDelete.add(analyticalProject, CIDOCCRM.P1_is_identified_by, title));
                    }

                    logger.info("Change label to [{}]", label);
                    modelToAdd.add(analyticalProject, CIDOCCRM.P1_is_identified_by, label);
                }

                if (description != null) {
                    Statement descriptionStatement = analyticalProjectModel.getProperty(analyticalProject, CIDOCCRM.P3_has_note);
                    if (descriptionStatement != null) {
                        modelToDelete.add(descriptionStatement);
                    }

                    logger.info("Change description from [{}] to [{}]", descriptionStatement != null ? descriptionStatement.getObject().asLiteral() : "", description);
                    modelToAdd.add(analyticalProject, CIDOCCRM.P3_has_note, description);
                }

                if (privacyTypeUuid != null) {
                    Statement privacyTypeStatement = analyticalProjectModel.getProperty(analyticalProject, Sherlock.has_privacy_type);
                    if (privacyTypeStatement != null) {
                        modelToDelete.add(privacyTypeStatement);
                    }

                    logger.info("Change privacy type from [{}] to [{}]", privacyTypeStatement != null ? privacyTypeStatement.getObject().asResource() : "", privacyTypeUuid);
                    modelToAdd.add(analyticalProject, Sherlock.has_privacy_type, modelToAdd.createResource(sherlock.makeIri(privacyTypeUuid)));
                }

                if (color != null) {
                    Model colorModel = getAnalyticalProjectColorE41P190(analyticalProject);
                    if (!colorModel.isEmpty()) {
                        Statement colorStatement = colorModel.listStatements().nextStatement();
                        modelToDelete.add(colorStatement);
                        modelToAdd.add(colorStatement.getSubject(), colorStatement.getPredicate(), color);
                    } else {
                        Resource e41 = modelToAdd.createResource(sherlock.makeIri());
                        modelToAdd.add(analyticalProject, CIDOCCRM.P1_is_identified_by, e41);
                        modelToAdd.add(e41, RDF.type, CIDOCCRM.E41_Appellation);
                        modelToAdd.add(e41, CIDOCCRM.P190_has_symbolic_content, color);
                        modelToAdd.add(e41, CIDOCCRM.P2_has_type, modelToAdd.createResource(sherlock.makeIri(e55HexColorUuid)));
                        logger.info("added color E41");
                    }
                    logger.info("Change color to [{}]", color);

                }

                conn.update(sherlock.makeDeleteQuery(modelToDelete));
                conn.update(sherlock.makeUpdateQuery(modelToAdd, sherlock.makeGraph(contributionGraph)));
            });
        }
        return modelToAdd;
    }
}
