package fr.cnrs.iremus.sherlock.service;

import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.external.authentication.OrcidUser;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfig;
import io.micronaut.context.annotation.Property;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import org.apache.jena.arq.querybuilder.ConstructBuilder;
import org.apache.jena.arq.querybuilder.SelectBuilder;
import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.ResultSetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdfconnection.RDFConnectionFuseki;
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder;
import org.apache.jena.sparql.engine.binding.Binding;
import org.apache.jena.sparql.resultset.ResultSetMem;
import org.apache.jena.vocabulary.DCTerms;
import org.apache.jena.vocabulary.RDF;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;

@Singleton
public class UserService {
    private final String e55EmojiUuid = "04242f64-fbb3-4b5b-bb2e-3ddd59eeea18";
    private final String e55HexColorUuid = "5f1bb74f-6ea0-4073-8b68-086f98454f1c";
    private final String e55OrcidNameUuid = "73ea8d74-3526-4f6a-8830-dd369795650d";
    private final String e55OrcidUuid = "d7ef2583-ff31-4913-9ed3-bc3a1c664b21";
    private final String[] userHexColors = new String[]{"5C2493", "470883", "33085C", "8E438D", "681B67", "DC6C7E", "CD4557", "9E303E", "F2AB84", "D58256", "B56439", "F2AB84", "FAE4AD", "EDCA77", "C49C44", "A1E6BB", "7DC597", "5AA373", "70B2B8", "3C8892", "2E6C74", "7B7B92", "515270", "3C3D53", "656183"};

    @Property(name = "jena")
    protected String jena;
    @Inject
    DateService dateService;
    @Inject
    Sherlock sherlock;

    /**
     * Create user if not exists in database
     *
     * @param orcidUser ORCID user who used OAuth2 protocol
     * @return E21_Person URI
     */
    public String createUserIfNotExists(OrcidUser orcidUser) {
        String userUuid = getUuidByOrcid(orcidUser.getSub());
        if (userUuid != null) return userUuid;

        String e21Iri = sherlock.makeIri();
        String e42Iri = sherlock.makeIri();
        String e55OrcidIri = sherlock.makeIri(e55OrcidUuid);
        String now = dateService.getNow();


        // BUILD MODEL
        Model m = ModelFactory.createDefaultModel();
        Resource e21_user = m.createResource(e21Iri);
        Resource e55_orcid = m.createResource(e55OrcidIri);
        Resource e42_identifier = m.createResource(e42Iri);

        m.add(e21_user, CIDOCCRM.P1_is_identified_by, e42_identifier);
        m.add(e21_user, RDF.type, CIDOCCRM.E21_Person);
        m.add(e21_user, DCTerms.created, now);
        m.add(e42_identifier, RDF.type, CIDOCCRM.E42_Identifier);
        m.add(e42_identifier, CIDOCCRM.P190_has_symbolic_content, orcidUser.getSub());
        m.add(e42_identifier, CIDOCCRM.P2_has_type, e55_orcid);

        // Assign default emoji and color to new users
        linkUserToEmoji(m, e21_user, null, getInitialsFromOrcidUser(orcidUser));
        linkUserToHexColor(m, e21_user, null, getRandomColor());
        linkUserToOrcidName(m, e21_user, null, getOrcidNameFromOrcidUser(orcidUser));

        String updateWithModel = sherlock.makeUpdateQuery(m, sherlock.getUserGraph());
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            // WRITE
            conn.update(updateWithModel);
            return e21_user.getURI();
        }
    }

    private String getOrcidNameFromOrcidUser(OrcidUser orcidUser) {
        return (Objects.toString(orcidUser.getGiven_name(),"") + " " + Objects.toString(orcidUser.getFamily_name(), "")).trim();
    }

    private String getRandomColor() {
        return userHexColors[ThreadLocalRandom.current().nextInt(userHexColors.length)];
    }

    private String getInitialsFromOrcidUser(OrcidUser orcidUser) {
        return String.valueOf(
                isNameNullOrEmpty(orcidUser.getGiven_name())
                        ? ""
                        : orcidUser.getGiven_name().charAt(0)
                ) + (
                isNameNullOrEmpty(orcidUser.getFamily_name())
                    ? ""
                    : orcidUser.getFamily_name().charAt(0)
                );
    }

    private boolean isNameNullOrEmpty(String name) {
        return name == null || name.isEmpty();
    }

    private String getUuidByOrcid(String orcid) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            SelectBuilder cb = new SelectBuilder()
                    .addVar("*")
                    .addGraph(sherlock.getUserGraph(), "?E42_Identifier", CIDOCCRM.P190_has_symbolic_content, orcid)
                    .addGraph(sherlock.getUserGraph(), "?E42_Identifier", RDF.type, CIDOCCRM.E42_Identifier)
                    .addGraph(sherlock.getUserGraph(), "?E21_Person", CIDOCCRM.P1_is_identified_by, "?E42_Identifier")
                    .addGraph(sherlock.getUserGraph(), "?E21_Person", RDF.type, CIDOCCRM.E21_Person);
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            ResultSetMem results = (ResultSetMem) ResultSetFactory.copyResults(qe.execSelect());
            return results.size() == 0 ? null : results.peek().get("?E21_Person").toString();
        }
    }

    public Resource getUserByUuid(String uuid) {
        Model m = ModelFactory.createDefaultModel();
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        Resource user = m.createResource(sherlock.makeIri(uuid));
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            SelectBuilder cb = new SelectBuilder()
                    .addVar("*")
                    .addGraph(sherlock.getUserGraph(), user, RDF.type, CIDOCCRM.E21_Person);
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            ResultSetMem results = (ResultSetMem) ResultSetFactory.copyResults(qe.execSelect());
            return results.size() == 0 ? null : user;
        }
    }

    public void editEmoji(Resource authenticatedUser, String emoji) {
        Model m = ModelFactory.createDefaultModel();
        Resource e55Emoji = m.createResource(sherlock.makeIri(e55EmojiUuid));
        editE41(m, emoji, authenticatedUser, e55Emoji);
    }

    public void editHexColor(Resource authenticatedUser, String hexColor) {
        Model m = ModelFactory.createDefaultModel();
        Resource e55HexColor = m.createResource(sherlock.makeIri(e55HexColorUuid));
        editE41(m, hexColor, authenticatedUser, e55HexColor);
    }

    private void editE41(Model model, String literal, Resource authenticatedUser, Resource e55) {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct("?E41_Appellation", CIDOCCRM.P190_has_symbolic_content, "?appellation")
                    .addGraph(sherlock.getUserGraph(), authenticatedUser, CIDOCCRM.P1_is_identified_by, "?E41_Appellation")
                    .addGraph(sherlock.getUserGraph(), "?E41_Appellation", RDF.type, CIDOCCRM.E41_Appellation)
                    .addGraph(sherlock.getUserGraph(), "?E41_Appellation", CIDOCCRM.P190_has_symbolic_content, "?appellation")
                    .addGraph(sherlock.getUserGraph(), "?E41_Appellation", CIDOCCRM.P2_has_type, e55);
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            Model modelRemoved = qe.execConstruct();
            conn.update(sherlock.makeDeleteQuery(modelRemoved, sherlock.getUserGraph()));
            linkUserToE41(model, authenticatedUser, modelRemoved.listSubjects().hasNext() ? modelRemoved.listSubjects().nextResource() : null, e55, literal);
            String updateWithModel = sherlock.makeUpdateQuery(model, sherlock.getUserGraph());
            conn.update(updateWithModel);
        }
    }


    public UserConfig getUserConfigByUuid(String uuid) {
        Model model = ModelFactory.createDefaultModel();
        UserConfig userConfig = new UserConfig();
        Resource e55Emoji = model.createResource(sherlock.makeIri(e55EmojiUuid));
        Resource e55HexColor = model.createResource(sherlock.makeIri(e55HexColorUuid));
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena);
        Resource user = model.createResource(sherlock.makeIri(uuid));
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            // WRITE
            SelectBuilder cb = new SelectBuilder()
                    .addVar("?type ?literal")
                    .addGraph(sherlock.getUserGraph(), user, CIDOCCRM.P1_is_identified_by, "?e41")
                    .addGraph(sherlock.getUserGraph(), "?e41", RDF.type, CIDOCCRM.E41_Appellation)
                    .addGraph(sherlock.getUserGraph(), "?e41", CIDOCCRM.P2_has_type, "?type")
                    .addGraph(sherlock.getUserGraph(), "?e41", CIDOCCRM.P190_has_symbolic_content, "?literal");
            Query q = cb.build();
            QueryExecution qe = conn.query(q);
            ResultSetMem results = (ResultSetMem) ResultSetFactory.copyResults(qe.execSelect());
            while (results.hasNext()) {
                Binding results1 = results.peekBinding();
                if (results1.get("type").toString().equals(e55Emoji.getURI()))
                    userConfig.setUnicodeChar(results1.get("literal").toString(false));
                if (results1.get("type").toString().equals(e55HexColor.getURI()))
                    userConfig.setHexColor(results1.get("literal").toString(false));
                results.next();
            }
            return userConfig;
        }
    }

    private void linkUserToEmoji(Model model, Resource e21_user, Resource e41_emoji, String emoji) {
        String e55EmojiIri = sherlock.makeIri(e55EmojiUuid);
        Resource e55Emoji = model.createResource(e55EmojiIri);
        linkUserToE41(model, e21_user, e41_emoji, e55Emoji, emoji);
    }

    private void linkUserToHexColor(Model model, Resource e21_user, Resource e41_emoji, String hexColor) {
        String e55HexColorIri = sherlock.makeIri(e55HexColorUuid);
        Resource e55HexColor = model.createResource(e55HexColorIri);
        linkUserToE41(model, e21_user, e41_emoji, e55HexColor, hexColor);
    }

    private void linkUserToOrcidName(Model model, Resource e21_user, Resource e41_name, String name) {
        String e55OrcidNameIri = sherlock.makeIri(e55OrcidNameUuid);
        Resource e55OrcidName = model.createResource(e55OrcidNameIri);
        linkUserToE41(model, e21_user, e41_name, e55OrcidName, name);
    }

    private void linkUserToE41(Model model, Resource e21_user, Resource e41, Resource e55, String literal) {
        if (e41 == null) e41 = model.createResource(sherlock.makeIri());
        model.add(e21_user, CIDOCCRM.P1_is_identified_by, e41);
        model.add(e41, RDF.type, CIDOCCRM.E41_Appellation);
        model.add(e41, CIDOCCRM.P190_has_symbolic_content, literal);
        model.add(e41, CIDOCCRM.P2_has_type, e55);
    }

}
