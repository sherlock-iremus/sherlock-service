package fr.cnrs.iremus.sherlock.analyticalEntity

import fr.cnrs.iremus.sherlock.Common;
import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.controller.AnalyticalEntityController
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import spock.lang.Specification;

@MicronautTest()
class AnalyticalEntityControllerSpec extends Specification {

    @Inject
    Common common

    void 'test post analytical entity creates triples'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-entity', [
                is_referred_to_by: ['http://data-iremus.huma-num/id/note-1',
                                     'http://data-iremus.huma-num/id/note-2',
                                     'http://data-iremus.huma-num/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique',
                e13s: [
                             p141: 'http://data-iremus.huma-num.fr/id/type-cadence',
                             p141_type: 'URI',
                             p177: 'http://www.cidoc-crm.org/cidoc-crm/P2_has_type',
                             document_context: 'http://data-iremus.huma-num/id/ma-partition',
                             analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique'
                 ]
        ])

        then:
        response[0]["@type"][0] == CIDOCCRM.E28_Conceptual_Object.URI
        response[0][CIDOCCRM.P2_has_type.URI][0]["@id"] == AnalyticalEntityController.e55analyticalEntityIri

        Model currentModel = common.getAllTriples()
        currentModel.contains(null, CIDOCCRM.P141_assigned, currentModel.createResource(response[0]['@id']))
        currentModel.contains(null, CIDOCCRM.P140_assigned_attribute_to, currentModel.createResource('http://data-iremus.huma-num/id/note-1') )
        currentModel.contains(null, CIDOCCRM.P140_assigned_attribute_to, currentModel.createResource('http://data-iremus.huma-num/id/note-2') )
        currentModel.contains(null, CIDOCCRM.P140_assigned_attribute_to, currentModel.createResource('http://data-iremus.huma-num/id/note-3') )
        currentModel.size() == 24
    }

    void 'test delete analytical entity, no more old triples left'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/analytical-entity', [
                is_referred_to_by: ['http://data-iremus.huma-num/id/note-1',
                                   'http://data-iremus.huma-num/id/note-2',
                                   'http://data-iremus.huma-num/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique',
                e13s: [
                        p141: 'http://data-iremus.huma-num.fr/id/type-cadence',
                        p141_type: 'URI',
                        p177: 'http://www.cidoc-crm.org/cidoc-crm/P2_has_type',
                        document_context: 'http://data-iremus.huma-num/id/ma-partition',
                        analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique'
                ]
        ])

        def analyticalEntityIri = postResponse[0]["@id"] as String
        def analyticalEntityUuid = analyticalEntityIri.split("/").last()

        def beforeDeleteModel = common.getAllTriples()

        common.delete("/sherlock/api/analytical-entity/${analyticalEntityUuid}")

        then:

        Model afterDeleteModel = common.getAllTriples()
        beforeDeleteModel.size() == 24
        afterDeleteModel.empty


    }

    void 'test deleting analytical entity used by other user fail'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/analytical-entity', [
                is_referred_to_by: ['http://data-iremus.huma-num/id/note-1',
                                    'http://data-iremus.huma-num/id/note-2',
                                    'http://data-iremus.huma-num/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique',
                e13s: [
                        p141: 'http://data-iremus.huma-num.fr/id/type-cadence',
                        p141_type: 'URI',
                        p177: 'http://www.cidoc-crm.org/cidoc-crm/P2_has_type',
                        document_context: 'http://data-iremus.huma-num/id/ma-partition',
                        analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique'
                ]
        ])


        def analyticalEntityIri = postResponse[0]["@id"] as String
        def analyticalEntityUuid = analyticalEntityIri.split("/").last()
        common.post('/sherlock/api/e13?fake-user=true', [
                "p140"              : analyticalEntityIri,
                "p177"              : "http://www.cidoc-crm.org/cidoc-crm/P2_has_type",
                "p141"              : "http://data-iremus.huma-num.fr/un-autre-type-de-cadence",
                "p141_type"         : "uri",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])


        common.delete("/sherlock/api/analytical-entity/${analyticalEntityUuid}")

        then:

        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.message == "Some resources belongs to other users."


    }


    void 'test deleting not existing analytical entity returns 404'() {
        when:
        common.eraseall()
        common.delete("/sherlock/api/analytical-entity/my-not-existing-analytical-entity")

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 404
    }
}
