package fr.cnrs.iremus.sherlock.functional

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import org.apache.jena.vocabulary.RDF
import spock.lang.Specification

@MicronautTest()
class AnalyticalEntityControllerSpec extends Specification {

    @Inject
    Common common

    @Inject
    Sherlock sherlock

    void 'test creating analytical entity and giving it Cadence Type creates all triples'() {
        when:
        common.eraseall()

        def analyticalEntityE55Iri = "http://data-iremus.huma-num.fr/id/6d72746a-9f28-4739-8786-c6415d53c56d"
        def simpleCadenceTypeIri = "http://modality-tonality.huma-num.fr/Zarlino_1558#SimpleCadence"

        def response = common.post('/sherlock/api/e13', [
                p140: ['http://data-iremus.huma-num.fr/id/note-1',
                                     'http://data-iremus.huma-num.fr/id/note-2',
                                     'http://data-iremus.huma-num.fr/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                "contribution_graph": "tonalities-contributions",
                p141_type: 'NEW_RESOURCE',
                p177: 'crm:P67_refers_to',
                new_p141 : [
                        rdf_type: ["crm:E28_Conceptual_Object"],
                        p2_type: [analyticalEntityE55Iri],
                ]
        ])

        def e28Iri = J.getOneByType(response, CIDOCCRM.E28_Conceptual_Object)["@id"]
        def response2 = common.post('/sherlock/api/e13', [
                p140: [e28Iri],
                document_context: 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                "contribution_graph": "tonalities-contributions",
                p141_type: 'URI',
                p177: 'crm:P2_has_type',
                p141: simpleCadenceTypeIri
        ])

        then:

        def model = common.getAllTriples()
        def e13AnalyticalEntityCreation = model.createResource(J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)["@id"])
        def e13AnalyticalEntityTypeAttribution = model.createResource(J.getOneByType(response2, CIDOCCRM.E13_Attribute_Assignment)["@id"])
        def analyticalEntity = model.listObjectsOfProperty(e13AnalyticalEntityCreation, CIDOCCRM.P141_assigned).next() as Resource

        model.contains(e13AnalyticalEntityCreation, RDF.type, CIDOCCRM.E13_Attribute_Assignment)
        model.contains(e13AnalyticalEntityCreation, CIDOCCRM.P140_assigned_attribute_to, model.createResource('http://data-iremus.huma-num.fr/id/note-1'))
        model.contains(e13AnalyticalEntityCreation, CIDOCCRM.P140_assigned_attribute_to, model.createResource('http://data-iremus.huma-num.fr/id/note-2'))
        model.contains(e13AnalyticalEntityCreation, CIDOCCRM.P140_assigned_attribute_to, model.createResource('http://data-iremus.huma-num.fr/id/note-3'))
        model.contains(e13AnalyticalEntityCreation, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P67_refers_to)
        model.contains(e13AnalyticalEntityCreation, Sherlock.has_document_context, model.createResource('http://data-iremus.huma-num.fr/id/ma-partition'))
        model.contains(e13AnalyticalEntityCreation, DCTerms.created, null)
        model.contains(e13AnalyticalEntityCreation, DCTerms.creator, model.createResource(sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")))
        model.contains(e13AnalyticalEntityCreation, CIDOCCRM.P14_carried_out_by, model.createResource(sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")))
        model.contains(e13AnalyticalEntityCreation, CIDOCCRM.P141_assigned, analyticalEntity)

        model.contains(analyticalEntity, RDF.type, CIDOCCRM.E28_Conceptual_Object)
        model.contains(analyticalEntity, CIDOCCRM.P2_has_type, model.createResource(analyticalEntityE55Iri))
        model.contains(analyticalEntity, DCTerms.created, null)
        model.contains(analyticalEntity, DCTerms.creator, model.createResource(sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")))

        model.contains(e13AnalyticalEntityTypeAttribution, RDF.type, CIDOCCRM.E13_Attribute_Assignment)
        model.contains(e13AnalyticalEntityTypeAttribution, CIDOCCRM.P140_assigned_attribute_to, analyticalEntity)
        model.contains(e13AnalyticalEntityTypeAttribution, CIDOCCRM.P177_assigned_property_of_type, CIDOCCRM.P2_has_type)
        model.contains(e13AnalyticalEntityTypeAttribution, Sherlock.has_document_context, model.createResource('http://data-iremus.huma-num.fr/id/ma-partition'))
        model.contains(e13AnalyticalEntityTypeAttribution, DCTerms.created, null)
        model.contains(e13AnalyticalEntityTypeAttribution, DCTerms.creator, model.createResource(sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")))
        model.contains(e13AnalyticalEntityTypeAttribution, CIDOCCRM.P14_carried_out_by, model.createResource(sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")))
        model.contains(e13AnalyticalEntityTypeAttribution, CIDOCCRM.P141_assigned, model.createResource(simpleCadenceTypeIri))

        model.contains(model.createResource("http://data-iremus.huma-num.fr/id/mon-projet-analytique"), CIDOCCRM.P9_consists_of , e13AnalyticalEntityCreation )
        model.contains(model.createResource("http://data-iremus.huma-num.fr/id/mon-projet-analytique"), CIDOCCRM.P9_consists_of , e13AnalyticalEntityTypeAttribution )

        model.size() == 24
    }

    void 'test deleting analytical entity removes all triples'() {
        when:
        common.eraseall()

        def analyticalEntityE55Iri = "http://data-iremus.huma-num.fr/id/6d72746a-9f28-4739-8786-c6415d53c56d"
        def simpleCadenceTypeIri = "http://modality-tonality.huma-num.fr/Zarlino_1558#SimpleCadence"

        def response = common.post('/sherlock/api/e13', [
                p140              : ['http://data-iremus.huma-num.fr/id/note-1',
                                     'http://data-iremus.huma-num.fr/id/note-2',
                                     'http://data-iremus.huma-num.fr/id/note-3'
                ],
                "contribution_graph": "tonalities-contributions",
                document_context  : 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                p141_type         : 'NEW_RESOURCE',
                p177              : 'crm:P67_refers_to',
                new_p141          : [
                        rdf_type: ["crm:E28_Conceptual_Object"],
                        p2_type : [analyticalEntityE55Iri],
                ]
        ])
        def e13AnalyticalEntityCreation = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)["@id"]
        def e28Iri = J.getOneByType(response, CIDOCCRM.E28_Conceptual_Object)["@id"]

        def response2 = common.post('/sherlock/api/e13', [
                p140              : [e28Iri],
                document_context  : 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                "contribution_graph": "tonalities-contributions",
                p141_type         : 'URI',
                p177              : 'crm:P2_has_type',
                p141              : simpleCadenceTypeIri
        ])
        def model = common.getAllTriples()

        def e13AnalyticalEntityTypeAttribution = J.getOneByType(response2, CIDOCCRM.E13_Attribute_Assignment)["@id"]

        common.delete("/sherlock/api/e13/${e13AnalyticalEntityTypeAttribution.toString().split("/").last()}")
        common.delete("/sherlock/api/e13/${e13AnalyticalEntityCreation.toString().split("/").last()}?propagate=true")


        def modelAfterDelete = common.getAllTriples()

        then:

        model.size() == 24
        modelAfterDelete.size() == 0
    }

    void 'test deleting analytical entity fails if resources depends on it'() {
        when:
        common.eraseall()

        def analyticalEntityE55Iri = "http://data-iremus.huma-num.fr/id/6d72746a-9f28-4739-8786-c6415d53c56d"
        def simpleCadenceTypeIri = "http://modality-tonality.huma-num.fr/Zarlino_1558#SimpleCadence"

        def response = common.post('/sherlock/api/e13', [
                p140              : ['http://data-iremus.huma-num.fr/id/note-1',
                                     'http://data-iremus.huma-num.fr/id/note-2',
                                     'http://data-iremus.huma-num.fr/id/note-3'
                ],
                document_context  : 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                "contribution_graph": "tonalities-contributions",
                p141_type         : 'NEW_RESOURCE',
                p177              : 'crm:P67_refers_to',
                new_p141          : [
                        rdf_type: ["crm:E28_Conceptual_Object"],
                        p2_type : [analyticalEntityE55Iri],
                ]
        ])
        def e13AnalyticalEntityCreation = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)["@id"]
        def e28Iri = J.getOneByType(response, CIDOCCRM.E28_Conceptual_Object)["@id"]

        common.post('/sherlock/api/e13', [
                p140              : [e28Iri],
                "contribution_graph": "tonalities-contributions",
                document_context  : 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                p141_type         : 'URI',
                p177              : 'crm:P2_has_type',
                p141              : simpleCadenceTypeIri
        ])
        common.delete("/sherlock/api/e13/${e13AnalyticalEntityCreation.toString().split("/").last()}?propagate=true")

        then:

        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
    }

}