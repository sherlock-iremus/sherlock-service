package fr.cnrs.iremus.sherlock.analyticalEntity

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.controller.AnalyticalEntityController
import fr.cnrs.iremus.sherlock.service.AnalyticalEntityService
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import spock.lang.Specification

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
        String p177Iri = sherlock.makeIri()
        String p140Iri = sherlock.makeIri()
        String annotationP177Iri = sherlock.makeIri()
        String annotationP141Iri = sherlock.makeIri()

        def response = common.post('/sherlock/api/analytical-entity', [
                'p177': p177Iri,
                'p140': p140Iri,
                'e13s': [
                        'p177': annotationP177Iri,
                        'p141': annotationP141Iri,
                        'p141_type': 'uri'
                ]
        ])

        then:
        response[0]["@type"][0] == CIDOCCRM.E28_Conceptual_Object.URI
        response[0][CIDOCCRM.P2_has_type.URI][0]["@id"] == AnalyticalEntityController.e55analyticalEntityIri
    }


    void 'test post analytical entity success if user does not provide annotations'() {
        when:
        common.eraseall()
        String p177Iri = sherlock.makeIri()
        String p140Iri = sherlock.makeIri()

        def response = common.post('/sherlock/api/analytical-entity', [
                'p177': p177Iri,
                'p140': p140Iri,
        ])

        then:

        response[0]["@type"][0] == CIDOCCRM.E28_Conceptual_Object.URI
        response[0][CIDOCCRM.P2_has_type.URI][0]["@id"] == AnalyticalEntityController.e55analyticalEntityIri
    }

    void 'test delete analytical entity without annotations, no more old triples left'() {
        when:
        common.eraseall()
        Model m = ModelFactory.createDefaultModel()

        String p177Iri = sherlock.makeIri()
        String p140Iri = sherlock.makeIri()

        def postResponse = common.post('/sherlock/api/analytical-entity', [
                'p177': p177Iri,
                'p140': p140Iri,
        ])

        def analyticalEntityIri = postResponse[0]["@id"] as String
        def analyticalEntityUuid = analyticalEntityIri.split("/").last()

        common.delete("/sherlock/api/analytical-entity/${analyticalEntityUuid}")

        then:

        Model currentModel = common.getAllTriples()
        currentModel.empty
    }

    void 'test delete analytical entity with annotations, no more old triples left'() {
        when:
        common.eraseall()
        Model m = ModelFactory.createDefaultModel()

        String p177Iri = sherlock.makeIri()
        String p140Iri = sherlock.makeIri()
        String annotationP177Iri = sherlock.makeIri()
        String annotationP141Iri = sherlock.makeIri()

        def postResponse = common.post('/sherlock/api/analytical-entity', [
                'p177': p177Iri,
                'p140': p140Iri,
                'e13s': [
                        'p177': annotationP177Iri,
                        'p141': annotationP141Iri,
                        'p141_type': 'uri'
                ]
        ])

        def analyticalEntityIri = postResponse[0]["@id"] as String
        def analyticalEntityUuid = analyticalEntityIri.split("/").last()

        common.delete("/sherlock/api/analytical-entity/${analyticalEntityUuid}")

        then:

        Model currentModel = common.getAllTriples()
        currentModel.empty
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
