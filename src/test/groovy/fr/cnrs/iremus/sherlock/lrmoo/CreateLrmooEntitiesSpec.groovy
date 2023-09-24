package fr.cnrs.iremus.sherlock.lrmoo

import fr.cnrs.iremus.sherlock.Common
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest()
class CreateLrmooEntitiesSpec extends Specification {
    @Inject
    @Client('/')
    Rx3HttpClient client

    @Inject
    Common common

    def meiFileUri = "https://raw.githubusercontent.com/sherlock-iremus/sherlock-service/master/data/Jos2701_CGN.mei"

    void 'test creation of a minimal LRMoo dataset from a MEI URI'() {
        given:
        common.eraseall()

        when:
        def response = common.post('/sherlock/api/lrmoo/mei-file-uri', ["fileUri": meiFileUri])

        then:
        println response
    }
}