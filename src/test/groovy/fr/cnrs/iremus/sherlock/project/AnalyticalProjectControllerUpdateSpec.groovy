package fr.cnrs.iremus.sherlock.project

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.controller.AnalyticalProjectController
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import spock.lang.Specification

@MicronautTest()
class AnalyticalProjectControllerUpdateSpec extends Specification {
    @Inject
    @Client('/')
    Rx3HttpClient client

    @Inject
    Common common

    @Inject
    Sherlock sherlock


    void 'test update minimal analytical project creates triples'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project', [
                label: 'Mon projet'
        ])
        def oldTriples = common.getAllTriples()
        def resource = response.find { item -> item["@type"][0] == CIDOCCRM.E7_Activity.URI }
        common.patch('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(resource["@id"]), [
                label: 'Mon projet modifié'
        ])
        then:

        def triples = common.getAllTriples()
        triples.listObjectsOfProperty(CIDOCCRM.P1_is_identified_by).next().asLiteral().toString() == 'Mon projet modifié'
        triples.size() == oldTriples.size()
    }

    void 'test update maximal analytical project creates triples'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project', [
                label: 'Mon projet'
        ])
        def resource = response.find { item -> item["@type"][0] == CIDOCCRM.E7_Activity.URI }
        common.patch('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(resource["@id"]), [
                label: 'Mon projet modifié',
                description: 'Ma description',
                color: 'FFFFFF',
                privacyTypeUuid: sherlock.getUuidFromSherlockUri(AnalyticalProjectController.e55publishedIri)
        ])
        then:

        def triples = common.getAllTriples()
        triples.listObjectsOfProperty(CIDOCCRM.P1_is_identified_by).filterKeep {r -> r.isLiteral()}.next().asLiteral().toString() == 'Mon projet modifié'
        triples.listObjectsOfProperty(CIDOCCRM.P3_has_note).next().asLiteral().toString() == 'Ma description'
        triples.listObjectsOfProperty(CIDOCCRM.P190_has_symbolic_content).next().asLiteral().toString() == 'FFFFFF'
        triples.listObjectsOfProperty(sherlock.has_privacy_type).next().asResource().toString() == AnalyticalProjectController.e55publishedIri
    }

    void 'test update then delete analytical entity removes all triples'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project', [
                label: 'Mon projet'
        ])
        def resource = response.find { item -> item["@type"][0] == CIDOCCRM.E7_Activity.URI }
        common.patch('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(resource["@id"]), [
                label: 'Mon projet modifié',
                description: 'Ma description',
                color: 'FFFFFF',
                privacyTypeUuid: sherlock.getUuidFromSherlockUri(AnalyticalProjectController.e55publishedIri)
        ])
        common.delete('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(resource["@id"]))

        then:

        def triples = common.getAllTriples()
        triples.size() == 0
    }

}
