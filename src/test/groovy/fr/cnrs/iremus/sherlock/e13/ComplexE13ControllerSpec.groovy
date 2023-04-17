package fr.cnrs.iremus.sherlock.e13

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

@MicronautTest
class ComplexE13ControllerSpec extends Specification {
    @Inject
    Common common
    @Inject
    Sherlock sherlock

    void 'test it works'() {
        when:
        common.eraseall()

        String annotatedResourceIri = "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"
        String annotationProperty = "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique"
        String documentContext = "http://data-iremus.huma-num/id/ma-partition"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def response = common.post('/sherlock/api/e13', [
                "p140"              : [annotatedResourceIri],
                "p177"              : annotationProperty,
                "p141_type" : "new resource",
                "new_p141"              : [
                    rdf_type: ["crm:E42_Identifier"],
                    p2_type: ["http://data-iremus.huma-num/id/identifiant-iiif", "http://data-iremus.huma-num/id/element-visuel"],
                    p190: "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : documentContext,
                "analytical_project": analyticalProject
        ])

        then:
        def e13 = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)

        J.getIri(e13, CIDOCCRM.P14_carried_out_by) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getIri(e13, CIDOCCRM.P140_assigned_attribute_to) == annotatedResourceIri
        J.getIri(e13, CIDOCCRM.P177_assigned_property_of_type) == annotationProperty

        def e42 = J.getOneByType(response, CIDOCCRM.E42_Identifier)

        J.getIri(e13, CIDOCCRM.P141_assigned) == e42["@id"]
        e42[CIDOCCRM.P2_has_type.URI]["@id"].contains("http://data-iremus.huma-num/id/identifiant-iiif")
        e42[CIDOCCRM.P2_has_type.URI]["@id"].contains("http://data-iremus.huma-num/id/element-visuel")
        J.getLiteralValue(e42, DCTerms.created)
        J.getIri(e42, DCTerms.creator) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getLiteralValue(e42, CIDOCCRM.P190_has_symbolic_content) == "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
    }

    void 'test creating complex e13 with wrong P141_type parameter fails'() {
        when:

        common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"],
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                "p141_type" : "URI",
                "new_p141"              : [
                        rdf_type: ["crm:E42_Identifier"],
                        p2_type: ["http://data-iremus.huma-num/id/identifiant-iiif", "http://data-iremus.huma-num/id/element-visuel"],
                        p190: "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 400
        e.message == "body: Please set either body.p141 or body.new_p141. And set corresponding p141_type"
    }

    void 'test creating incomplete complex e13 fails'() {
        when:

        common.post('/sherlock/api/e13', [
                "p140"              : ["http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"],
                "p177"              : "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique",
                "p141_type" : "new resource",
                "new_p141"              : [
                        rdf_type: [],
                        p2_type: ["http://data-iremus.huma-num/id/identifiant-iiif", "http://data-iremus.huma-num/id/element-visuel"],
                        p190: "https://ceres.huma-num.fr/iiif/3/mercure-galant-estampes--1677-09_224/600,100,300,60/max/0/default.jpg"
                ],
                "document_context"  : "http://data-iremus.huma-num/id/ma-partition",
                "analytical_project": "http://data-iremus.huma-num/id/mon-projet-analytique"
        ])

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 400
    }
}