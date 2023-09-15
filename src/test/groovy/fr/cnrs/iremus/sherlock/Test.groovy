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

        String annotatedResourceIri = "http://data-iremus.huma-num/id/e36-estampe"
        String annotationProperty = "http://data-iremus.huma-num/id/titre-sur-l-image"
        String annotationValue = "Et Nostris Prævalet Alis. Iam Supra Sublimia"
        String documentContext = "http://data-iremus.huma-num/id/e36-estampe"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def response = common.post('/sherlock/api/e13', [
                "p140"              : annotatedResourceIri,
                "p177"              : annotationProperty,
                "p141"              : annotationValue,
                "p141_type"         : "literal",
                "document_context"  : documentContext,
                "analytical_project": analyticalProject
        ])

        then:
        println response
//        "Hello World" == response["message"]
    }
}
