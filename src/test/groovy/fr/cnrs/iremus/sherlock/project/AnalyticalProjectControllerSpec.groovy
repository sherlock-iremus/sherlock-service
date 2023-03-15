package fr.cnrs.iremus.sherlock.project

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.common.CIDOCCRM;
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.controller.AnalyticalProjectController
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification;

@MicronautTest()
class AnalyticalProjectControllerSpec extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client

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
                label: 'Mon projet'
        ])

        then:response

        def resource = response.find { item -> item["@type"][0] ==  CIDOCCRM.E7_Activity.URI }
        def timeSpan = response.find { item -> item["@type"][0] ==  CIDOCCRM.E52_Time_span.URI }

        resource["@type"][0] == CIDOCCRM.E7_Activity.URI
        resource[CIDOCCRM.P1_is_identified_by.URI]["@value"][0] == 'Mon projet'
        resource[CIDOCCRM.P2_has_type.URI][0]["@id"] == AnalyticalProjectController.e55analyticalProjectIri
        resource[Sherlock.has_privacy_type.URI][0]["@id"] == AnalyticalProjectController.e55draftIri
        resource[CIDOCCRM.P14_carried_out_by.URI][0]["@id"] == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        resource[CIDOCCRM.P4_has_time_span.URI][0]["@id"] == timeSpan["@id"]

        timeSpan["@type"][0] == CIDOCCRM.E52_Time_span.URI
        dateService.isValidISODateTime(timeSpan[CIDOCCRM.P82a_begin_of_the_begin.URI][0]["@value"])
    }
}
