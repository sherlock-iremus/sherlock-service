package fr.cnrs.iremus.sherlock.functional

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import spock.lang.Specification

@MicronautTest()
class VisualItemAnnotationSpec extends Specification {

    @Inject
    Common common

    @Inject
    Sherlock sherlock

    public final static String e55ImageFragmentIri = "http://data-iremus.huma-num.fr/id/69a87e42-fa1e-46d1-9ae1-eb73fb0894d8"
    public final static String e55IiifIri = "http://data-iremus.huma-num.fr/id/19073c4a-0ef7-4ac4-a51a-e0810a596773"

    void "Indexation de la thématique 'Religion' sur l'estampe 1677-09_224"() {
        when:
        common.eraseall()

        def estampeE36Iri = "http://data-iremus.huma-num.fr/id/24c7c452-e9cf-4280-bb23-a66744f74835"
        def thematiqueIndexationE55Iri = "http://data-iremus.huma-num.fr/id/f2d9b792-2cfd-4265-a2c5-e0a69ce01536"
        def religionE55Iri = "https://opentheso.huma-num.fr/opentheso/?idc=religion&idt=th391"
        def analyticalProjectIri = "http://data-iremus.huma-num.fr/id/756aa164-0cde-46ac-bc3a-a0ea83a08e2d"

        def response = common.post('/sherlock/api/e13', [
                "p140"              : [estampeE36Iri],
                "p177"              : thematiqueIndexationE55Iri,
                "p141"              : religionE55Iri,
                "p141_type"         : "URI",
                "document_context"  : estampeE36Iri,
                "analytical_project": analyticalProjectIri,
                "contribution_graph": "tonalities-contributions"
        ])

        def model = common.getAllTriples()
        def e13 = model.createResource(J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)["@id"])

        then:
        def user = model.createResource(sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327"))
        model.contains(e13, RDF.type, CIDOCCRM.E13_Attribute_Assignment)
        model.contains(e13, DCTerms.creator, user)
        model.contains(e13, DCTerms.created, null)
        model.contains(e13, RDF.type, CIDOCCRM.E13_Attribute_Assignment)
        model.contains(e13, CIDOCCRM.P14_carried_out_by, user)
        model.contains(e13, CIDOCCRM.P140_assigned_attribute_to, model.createResource(estampeE36Iri))
        model.contains(e13, CIDOCCRM.P177_assigned_property_of_type, model.createResource(thematiqueIndexationE55Iri))
        model.contains(e13, CIDOCCRM.P141_assigned, model.createResource(religionE55Iri))
        model.contains(model.createResource(analyticalProjectIri), CIDOCCRM.P9_consists_of, e13)
        model.size() == 9
    }

    void "Indexation de la représentation de la personne 'Louis XIV' sur un fragment de l'estampe 1677-09_224"() {
        when:
        common.eraseall()
        Model m = ModelFactory.createDefaultModel()

        def estampeE36Iri = "http://data-iremus.huma-num.fr/id/24c7c452-e9cf-4280-bb23-a66744f74835"
        def louisXIVE21Iri = "http://data-iremus.huma-num.fr/sherlock/id/1480e2ab-f3d6-4915-a0c0-9a5b55fd56b6"
        def analyticalProjectIri = "http://data-iremus.huma-num.fr/id/756aa164-0cde-46ac-bc3a-a0ea83a08e2d"
        def iiifFragmentURL = "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
        def estampeE36 = m.createResource(estampeE36Iri)

        // Add this triple to mirror production dataset state.
        common.addTripleToDataset(estampeE36, RDF.type, CIDOCCRM.E36_Visual_Item)

        def response = common.post('/sherlock/api/e90/fragment', [
                "parent"              : estampeE36Iri,
                "p2_type" : [e55ImageFragmentIri],
        ])

        def e36FragmentIri = J.getOneByType(response, CIDOCCRM.E36_Visual_Item)["@id"]
        def responsePostE13Indexation = common.post('/sherlock/api/e13', [
                "p140"              : [e36FragmentIri],
                "p177"              : CIDOCCRM.P138_represents.URI,
                "p141"              : louisXIVE21Iri,
                "p141_type"         : "URI",
                "document_context"  : estampeE36Iri,
                "contribution_graph": "tonalities-contributions",
                "analytical_project": analyticalProjectIri
        ])

        def responsePostE13IdentifierAttribution = common.post('/sherlock/api/e13', [
                "p140"              : [e36FragmentIri],
                "p177"              : CIDOCCRM.P1_is_identified_by.URI,
                "new_p141"              : [
                        rdf_type: ["crm:E42_Identifier"],
                        p2_type: [e55IiifIri],
                        p190: iiifFragmentURL
                ],
                "p141_type"         : "NEW_RESOURCE",
                "contribution_graph": "tonalities-contributions",
                "document_context"  : estampeE36Iri,
                "analytical_project": analyticalProjectIri
        ])

        then:

        def model = common.getAllTriples()
        def user = model.createResource(sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327"))
        def estampeFragment = model.createResource(e36FragmentIri)
        def e13Indexation = model.createResource(J.getOneByType(responsePostE13Indexation, CIDOCCRM.E13_Attribute_Assignment)["@id"])
        def e13IdentifierAttribution = model.createResource(J.getOneByType(responsePostE13IdentifierAttribution, CIDOCCRM.E13_Attribute_Assignment)["@id"])
        def e42Identifier = model.createResource(J.getOneByType(responsePostE13IdentifierAttribution, CIDOCCRM.E42_Identifier)["@id"])
        def analyticalProject = model.createResource(analyticalProjectIri)

        model.contains(estampeE36, RDF.type, CIDOCCRM.E36_Visual_Item)

        model.contains(estampeFragment, RDF.type, CIDOCCRM.E36_Visual_Item)
        model.contains(estampeFragment, CIDOCCRM.P106i_forms_part_of, estampeE36)
        model.contains(estampeFragment, CIDOCCRM.P2_has_type, model.createResource(e55ImageFragmentIri))
        model.contains(estampeFragment, DCTerms.created)
        model.contains(estampeFragment, DCTerms.creator, user)
        model.contains(estampeFragment, DCTerms.creator, user)

        model.contains(e13Indexation, RDF.type, CIDOCCRM.E13_Attribute_Assignment)
        model.contains(e13Indexation, DCTerms.creator, user)
        model.contains(e13Indexation, DCTerms.created)
        model.contains(e13Indexation, CIDOCCRM.P14_carried_out_by, user)
        model.contains(e13Indexation, CIDOCCRM.P140_assigned_attribute_to, estampeFragment)
        model.contains(e13Indexation, CIDOCCRM.P141_assigned, model.createResource(louisXIVE21Iri))
        model.contains(e13Indexation, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P138_represents)
        model.contains(e13Indexation, Sherlock.has_document_context, estampeE36)

        model.contains(e13IdentifierAttribution, RDF.type, CIDOCCRM.E13_Attribute_Assignment)
        model.contains(e13IdentifierAttribution, DCTerms.creator, user)
        model.contains(e13IdentifierAttribution, DCTerms.created)
        model.contains(e13IdentifierAttribution, CIDOCCRM.P14_carried_out_by, user)
        model.contains(e13IdentifierAttribution, CIDOCCRM.P140_assigned_attribute_to, estampeFragment)
        model.contains(e13IdentifierAttribution, CIDOCCRM.P141_assigned, e42Identifier)
        model.contains(e13IdentifierAttribution, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P1_is_identified_by)
        model.contains(e13IdentifierAttribution, Sherlock.has_document_context, estampeE36)

        model.contains(e42Identifier, RDF.type, CIDOCCRM.E42_Identifier)
        model.contains(e42Identifier, DCTerms.creator, user)
        model.contains(e42Identifier, DCTerms.created)
        model.contains(e42Identifier, CIDOCCRM.P190_has_symbolic_content, iiifFragmentURL)

        model.contains(analyticalProject, CIDOCCRM.P9_consists_of, e13Indexation)
        model.contains(analyticalProject, CIDOCCRM.P9_consists_of, e13IdentifierAttribution)

        model.size() == 29
    }
}
