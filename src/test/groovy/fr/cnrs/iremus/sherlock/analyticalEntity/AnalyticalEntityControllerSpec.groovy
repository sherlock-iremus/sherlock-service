package fr.cnrs.iremus.sherlock.analyticalEntity

import fr.cnrs.iremus.sherlock.Common;
import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.controller.AnalyticalEntityController;
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
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
}
