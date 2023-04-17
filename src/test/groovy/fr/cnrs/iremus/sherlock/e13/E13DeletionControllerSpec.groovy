package fr.cnrs.iremus.sherlock.e13

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class E13DeletionControllerSpec extends Specification {
    @Inject
    Common common

    void 'test deleting E13 does not work if another resource depends on the P141'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence",
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                "p141"              : "http://data-iremus.huma-num/id/mon-commentaire",
                "p141_type"         : "uri",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        def e13Iri = postResponse[0]["@id"] as String
        def e13Uuid = e13Iri.split("/").last()

        common.post('/sherlock/api/e13', [
                "p140"              : "http://data-iremus.huma-num/id/mon-commentaire",
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-commentaire",
                "p141"              : "Si !",
                "p141_type"         : "literal",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])
        common.delete("/sherlock/api/e13/${e13Uuid}")

        then:

        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.message == "Please delete entities which depends on the P141 of the E13 first."
    }

    void 'test deleting E13 does not work if another resource depends on the E13'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence",
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                "p141"              : "http://data-iremus.huma-num/id/mon-commentaire",
                "p141_type"         : "uri",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        def e13Iri = postResponse[0]["@id"] as String
        def e13Uuid = e13Iri.split("/").last()

        common.post('/sherlock/api/e13?fake-user=true', [
                "p140"              : "http://data-iremus.huma-num/id/mon-commentaire",
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-commentaire",
                "p141"              : e13Iri,
                "p141_type"         : "literal",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        common.delete("/sherlock/api/e13/${e13Uuid}?propagate=true")

        then:

        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.message == "Please delete entities which depends on the P141 of the E13 first."
    }

    void 'test deleting E13 propagates if the p141 belongs to current user'() {
        common.eraseall()
        when:

        def response = common.post('/sherlock/api/e13', [
                p140: ['http://data-iremus.huma-num.fr/id/note-1',
                       'http://data-iremus.huma-num.fr/id/note-2',
                       'http://data-iremus.huma-num.fr/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                p141_type: 'new resource',
                p177: 'crm:P67_refers_to',
                new_p141 : [
                        rdf_type: ["crm:E28_Conceptual_Object"],
                        p2_type: ["http://data-iremus.huma-num.fr/id/analytical-entity-e55"],
                ]
        ])

        def beforeDeleteModel = common.getAllTriples()
        def e13Iri = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)["@id"]

        common.delete("/sherlock/api/e13/${e13Iri.split("/").last()}?propagate=true")
        def afterDeleteModel = common.getAllTriples()
        then:

        beforeDeleteModel.size() == 15
        afterDeleteModel.empty
    }

    void 'test deleting E13 does not propagate if the p141 belongs to another user'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence",
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                "p141"              : "http://data-iremus.huma-num/id/mon-commentaire",
                "p141_type"         : "uri",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        def e13Iri = postResponse[0]["@id"] as String

        postResponse = common.post('/sherlock/api/e13?fake-user=true', [
                "p140"              : "http://data-iremus.huma-num/id/mon-commentaire",
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-commentaire",
                "p141"              : e13Iri,
                "p141_type"         : "literal",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        e13Iri = postResponse[0]["@id"] as String
        def e13Uuid = e13Iri.split("/").last()
        def beforeDeleteModel = common.getAllTriples()

        common.delete("/sherlock/api/e13/${e13Uuid}?propagate=true&fake-user=true")

        then:
        def afterDeleteModel = common.getAllTriples()
        beforeDeleteModel.size() == 18
        afterDeleteModel.size() == 9
    }

    void 'test deleting E13 without propagate parameter does not propagate '() {
        common.eraseall()
        when:

        def response = common.post('/sherlock/api/e13', [
                p140: ['http://data-iremus.huma-num.fr/id/note-1',
                       'http://data-iremus.huma-num.fr/id/note-2',
                       'http://data-iremus.huma-num.fr/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num.fr/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num.fr/id/mon-projet-analytique',
                p141_type: 'new resource',
                p177: 'crm:P67_refers_to',
                new_p141 : [
                        rdf_type: ["crm:E28_Conceptual_Object"],
                        p2_type: ["http://data-iremus.huma-num.fr/id/analytical-entity-e55"],
                ]
        ])

        def beforeDeleteModel = common.getAllTriples()
        def e13Iri = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)["@id"]

        common.delete("/sherlock/api/e13/${e13Iri.split("/").last()}")
        def afterDeleteModel = common.getAllTriples()
        then:

        beforeDeleteModel.size() == 15
        afterDeleteModel.size() == 4
    }

}