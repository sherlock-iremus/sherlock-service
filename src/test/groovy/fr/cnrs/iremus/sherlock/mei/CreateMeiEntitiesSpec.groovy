package fr.cnrs.iremus.sherlock.mei

import fr.cnrs.iremus.sherlock.Common
import groovy.json.JsonOutput
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest()
class CreateMeiEntitiesSpec extends Specification {
    @Inject
    @Client('/')
    Rx3HttpClient client

    @Inject
    Common common

    def meiFileUrl1 = "https://raw.githubusercontent.com/polifonia-project/tonalities_pilot/main/scores/Hellinck/Hellinck_Beati.mei"

    void 'Get MEI header data from a staticcaly published MEI file'() {
        given:
        common.eraseall()

        when:
        def response = common.post('/sherlock/api/mei/head', ["file_url": meiFileUrl1])

        then:
        def j = JsonOutput.prettyPrint(JsonOutput.toJson(response))
        "Beati omnes qui timent Dominum" == response["meiHead"]["fileDesc"]["titleStmt"]["title"]
    }
}