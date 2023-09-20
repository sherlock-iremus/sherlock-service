package fr.cnrs.iremus.sherlock

import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class Test extends Specification {

    @Inject
    Common common

    @Inject
    ObjectMapper objectMapper

    void 'test it works'() {
        when:
//        def response = common.get('/sherlock/test')

        def response = common.post('/sherlock/api/e13',
                [
                        "p140"              : ["http://data-iremus.huma-num/id/e36-estampe"],

                        "p177"              : "http://data-iremus.huma-num/id/titre-sur-l-image",
                        "p141"              : "Et Nostris Prævalet Alis. Iam Supra Sublimia",
                        "p141_type"         : "LITERAL",
                        "document_context"  : "http://data-iremus.huma-num/id/e36-estampe",
                        "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
                ])

        then:
        println "coucou"
//        "Hello World" == response["message"]
    }
}
