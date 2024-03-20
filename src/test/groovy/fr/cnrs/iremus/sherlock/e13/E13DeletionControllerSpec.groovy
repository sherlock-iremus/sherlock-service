package fr.cnrs.iremus.sherlock.e13

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.controller.E13Controller
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Property
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import spock.lang.Specification

@MicronautTest
class E13DeletionControllerSpec extends Specification {
    @Inject
    Common common
    @Inject
    Sherlock sherlock

    void 'test deleting E13 that does not exists fails'() {
        common.eraseall()
        when:
        common.delete("/sherlock/api/e13/undefined")
        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 404
        e.getMessage() == "This E13 does not exist."

    }

    void 'test deleting E13 with incoming triples fails'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"],
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                "p141"              : "http://data-iremus.huma-num/id/mon-commentaire",
                "p141_type"         : "URI",
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        def e13Iri = J.getOneByType(postResponse, CIDOCCRM.E13_Attribute_Assignment)["@id"] as String
        def e13uuid = e13Iri.split("/").last()
        common.addTripleToDataset(common.createResource("s"), common.createProperty("p"), common.createResource(e13Iri))
        common.delete("/sherlock/api/e13/${e13uuid}")

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.getMessage() == "This E13 has incoming triples. Delete them."
    }

    void 'test deleting E13 with P141 having incoming triples fails'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                "p177"              : CIDOCCRM.P67_refers_to.URI,
                "p141_type"         : "NEW_RESOURCE",
                "new_p141"          : [
                        rdf_type: [CIDOCCRM.E28_Conceptual_Object.URI],
                        p2_type : ["http://data-iremus.huma-num.fr/id/identifiant-iiif", "http://data-iremus.huma-num.fr/id/element-visuel"],
                        p190    : "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num.fr/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
        ])
        def e13Iri = J.getOneByType(postResponse, CIDOCCRM.E13_Attribute_Assignment)["@id"] as String
        def e13uuid = e13Iri.split("/").last()
        def e28Iri = J.getOneByType(postResponse, CIDOCCRM.E28_Conceptual_Object)["@id"] as String
        common.addTripleToDataset(common.createResource("s"), common.createProperty("p"), common.createResource(e28Iri))
        common.delete("/sherlock/api/e13/${e13uuid}")

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.getMessage() == E13Controller.E13_DELETE_PLEASE_ENTITIES_FIRST
    }

    void 'test deleting E13 with P67 as P177 works'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                "p177"              : CIDOCCRM.P67_refers_to.URI,
                "p141_type"         : "NEW_RESOURCE",
                "new_p141"          : [
                        rdf_type: [CIDOCCRM.E28_Conceptual_Object.URI],
                        p2_type : ["http://data-iremus.huma-num.fr/id/identifiant-iiif", "http://data-iremus.huma-num.fr/id/element-visuel"],
                        p190    : "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num.fr/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
        ])
        def e13Iri = J.getOneByType(postResponse, CIDOCCRM.E13_Attribute_Assignment)["@id"] as String
        def e13uuid = e13Iri.split("/").last()
        def beforeDeleteModelSize = common.getAllTriples().size()
        common.delete("/sherlock/api/e13/${e13uuid}")
        def afterDeleteModelSize = common.getAllTriples().size()

        then:
        afterDeleteModelSize == 0
        beforeDeleteModelSize == 15
    }

    void 'test deleting not owned E13 without P67 as P177 fails'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                "p177"              : CIDOCCRM.P1_is_identified_by.URI,
                "p141_type"         : "NEW_RESOURCE",
                "new_p141"          : [
                        rdf_type: [CIDOCCRM.E28_Conceptual_Object.URI],
                        p2_type : ["http://data-iremus.huma-num.fr/id/identifiant-iiif", "http://data-iremus.huma-num.fr/id/element-visuel"],
                        p190    : "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num.fr/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
        ])
        def e13Iri = J.getOneByType(postResponse, CIDOCCRM.E13_Attribute_Assignment)["@id"] as String
        def e13uuid = e13Iri.split("/").last()
        common.delete("/sherlock/api/e13/${e13uuid}?fake-user=true")

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.getMessage() == E13Controller.E13_DELETE_BELONGS_TO_ANOTHER_USER
    }

    void 'test deleting owned E13 and its linked Resource works'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                "p177"              : CIDOCCRM.P1_is_identified_by.URI,
                "p141_type"         : "NEW_RESOURCE",
                "new_p141"          : [
                        rdf_type: [CIDOCCRM.E28_Conceptual_Object.URI],
                        p2_type : ["http://data-iremus.huma-num.fr/id/identifiant-iiif", "http://data-iremus.huma-num.fr/id/element-visuel"],
                        p190    : "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num.fr/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
        ])
        def e13Iri = J.getOneByType(postResponse, CIDOCCRM.E13_Attribute_Assignment)["@id"] as String
        def e13uuid = e13Iri.split("/").last()
        def beforeDeleteModelSize = common.getAllTriples().size()
        common.delete("/sherlock/api/e13/${e13uuid}")
        def afterDeleteModelSize = common.getAllTriples().size()
        println(common.getAllTriples())

        then:
        beforeDeleteModelSize == 15
        afterDeleteModelSize == 0

    }

    void 'test deleting not owned E13 fails'() {
        when:
        common.eraseall()

        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                "p177"              : CIDOCCRM.P67_refers_to.URI,
                "p141_type"         : "NEW_RESOURCE",
                "new_p141"          : [
                        rdf_type: [CIDOCCRM.E28_Conceptual_Object.URI],
                        p2_type : ["http://data-iremus.huma-num.fr/id/identifiant-iiif", "http://data-iremus.huma-num.fr/id/element-visuel"],
                        p190    : "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num.fr/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
        ])
        def e13Iri = J.getOneByType(postResponse, CIDOCCRM.E13_Attribute_Assignment)["@id"] as String
        def e13uuid = e13Iri.split("/").last()
        def beforeDeleteModelSize = common.getAllTriples().size()
        common.delete("/sherlock/api/e13/${e13uuid}?fake-user=true")

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.getMessage() == E13Controller.E13_DELETE_BELONGS_TO_ANOTHER_USER
    }

    void 'test deleting an e13 with has a pre-existing p141 throws 200 but does not delete p141\'s triples'() {
        when:

        common.eraseall()
        def p141Uri = "http://data-iremus.huma-num/id/e28-de-quelqu-un-d-autre"
        def postResponse = common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num.fr/id/e13-assignant-le-type-cadence"],
                "p177"              : CIDOCCRM.P67_refers_to.URI,
                "p141"              : p141Uri,
                "p141_type"         : "URI",
                "document_context"  : "http://data-iremus.huma-num.fr/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num.fr/id/mon-projet-analytique"
        ])

        def e13Iri = J.getOneByType(postResponse, CIDOCCRM.E13_Attribute_Assignment)["@id"] as String
        def e13uuid = e13Iri.split("/").last()
        common.addTripleToDataset(common.createResource(p141Uri), common.createProperty("p"), common.createResource("o"))
        common.delete("/sherlock/api/e13/${e13uuid}")

        then:
        common.getAllTriples().size() == 1
    }
}