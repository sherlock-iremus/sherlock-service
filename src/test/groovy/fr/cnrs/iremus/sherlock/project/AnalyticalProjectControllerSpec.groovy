package fr.cnrs.iremus.sherlock.project

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.controller.AnalyticalProjectController
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest()
class AnalyticalProjectControllerSpec extends Specification {

    @Inject
    @Client('/')
    Rx3HttpClient client

    @Inject
    Common common

    @Inject
    Sherlock sherlock

    @Inject
    DateService dateService

    void 'test create minimal analytical project creates triples'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project', [
                label: 'Mon projet',
                "contribution_graph": "tonalities-contributions",
        ])

        then:
        response

        def resource = response.find { item -> item["@type"][0] == CIDOCCRM.E7_Activity.URI }
        def timeSpan = response.find { item -> item["@type"][0] == CIDOCCRM.E52_Time_span.URI }

        resource["@type"][0] == CIDOCCRM.E7_Activity.URI
        resource[CIDOCCRM.P1_is_identified_by.URI]["@value"][0] == 'Mon projet'
        resource[CIDOCCRM.P2_has_type.URI][0]["@id"] == AnalyticalProjectController.e55analyticalProjectIri
        resource[Sherlock.has_privacy_type.URI][0]["@id"] == AnalyticalProjectController.e55draftIri
        resource[CIDOCCRM.P14_carried_out_by.URI][0]["@id"] == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        resource[CIDOCCRM.P4_has_time_span.URI][0]["@id"] == timeSpan["@id"]

        timeSpan["@type"][0] == CIDOCCRM.E52_Time_span.URI
        dateService.isValidISODateTime(timeSpan[CIDOCCRM.P82a_begin_of_the_begin.URI][0]["@value"])
    }

    void 'test deleting minimal analytical project deletes triples'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project', [
                label: 'Mon projet',
                "contribution_graph": "tonalities-contributions"
        ])

        common.delete('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(J.getOneByType(response, CIDOCCRM.E7_Activity)["@id"]))

        then:

        common.getAllTriples().size() == 0
    }

    void 'test deleting an other other\'s analytical project triggers error'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project?fake-user=true', [
                label: 'Mon projet',
                "contribution_graph": "tonalities-contributions",
        ])

        common.delete('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(J.getOneByType(response, CIDOCCRM.E7_Activity)["@id"]))

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.getMessage() == AnalyticalProjectController.ANALYTICAL_PROJECT_BELONGS_TO_ANOTHER_USER
    }

    void 'test deleting a filled analytical project with other users\'s e13 works'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project', [
                label: 'Mon projet',
                "contribution_graph": "tonalities-contributions"
        ])
        def analyticalProjectUri = J.getOneByType(response, CIDOCCRM.E7_Activity)["@id"]

        common.post('/sherlock/api/e13?fake-user=true', [
                "p140"              : ["test"],
                "p177"              : "test",
                "p141_type"         : "NEW_RESOURCE",
                "new_p141"          : [
                        rdf_type: ["crm:E42_Identifier"],
                        p2_type : ["http://data-iremus.huma-num/id/identifiant-iiif", "http://data-iremus.huma-num/id/element-visuel"],
                        p190    : "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "test",
                "contribution_graph": "tonalities-contributions",
                "analytical_project": analyticalProjectUri
        ])
        common.delete('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(analyticalProjectUri))

        then:
        common.getAllTriples().size() == 0
    }

    void 'test deleting analytical project with an E13 with P141 having incoming triples works'() {
        when:
        common.eraseall()

        def response = common.post('/sherlock/api/analytical-project', [
                label: 'Mon projet',
                "contribution_graph": "tonalities-contributions",
        ])
        def analyticalProjectUri = J.getOneByType(response, CIDOCCRM.E7_Activity)["@id"]

        def e13Response = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                "p177"              : CIDOCCRM.P67_refers_to.URI,
                "p141_type"         : "NEW_RESOURCE",
                "contribution_graph": "tonalities-contributions",
                "new_p141"          : [
                        rdf_type: [CIDOCCRM.E28_Conceptual_Object.URI],
                        p2_type : ["http://data-iremus.huma-num.fr/id/identifiant-iiif", "http://data-iremus.huma-num.fr/id/element-visuel"],
                        p190    : "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num.fr/id/ma-partition",
                "analytical_project": analyticalProjectUri
        ])

        def e28Iri = J.getOneByType(e13Response, CIDOCCRM.E28_Conceptual_Object)["@id"] as String
        common.addTripleToDataset(common.createResource("http://data-iremus.huma-num.fr/id/s"), common.createProperty("http://data-iremus.huma-num.fr/id/p"), common.createResource(e28Iri))

        common.delete('/sherlock/api/analytical-project/' + sherlock.getUuidFromSherlockUri(analyticalProjectUri))


        then:
        common.getAllTriples().size() == 1

    }
}
