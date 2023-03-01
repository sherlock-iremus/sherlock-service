package fr.cnrs.iremus.sherlock.analyticalEntity

import fr.cnrs.iremus.sherlock.Common;
import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.controller.AnalyticalEntityController
import fr.cnrs.iremus.sherlock.service.AnalyticalEntityService;
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.RDF
import spock.lang.Specification;

@MicronautTest()
class AnalyticalEntityControllerSpec extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    Common common

    @Inject
    Sherlock sherlock

    @Inject
    AnalyticalEntityService analyticalEntityService

    void 'test post analytical entity creates triples'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-entity', [
                referredEntities: ['http://data-iremus.huma-num/id/note-1',
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
        currentModel.size() == 22
    }

    void 'test delete analytical entity, no more old triples left'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/analytical-entity', [
                referredEntities: ['http://data-iremus.huma-num/id/note-1',
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
        beforeDeleteModel.size() == 22
        afterDeleteModel.empty


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
