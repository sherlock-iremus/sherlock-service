package fr.cnrs.iremus.sherlock.e13

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.ValidateUUID
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

@MicronautTest
class E13ControllerSpec extends Specification {
    @Inject
    Common common
    @Inject
    DateService dateService
    @Inject
    Sherlock sherlock

    void 'test it works'() {
        when:
        common.eraseall()

        String annotatedResourceIri = "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"
        String annotationProperty = "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique"
        String annotationValue = "Ce n'est pas une cadence."
        String documentContext = "http://data-iremus.huma-num/id/ma-partition"
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
        response.size == 1
        response[0]["@id"].startsWith(sherlock.getResourcePrefix())
        ValidateUUID.isValid(response[0]["@id"].split("/").last())
        sherlock.resolvePrefix(response[0]["@type"]) == CIDOCCRM.E13_Attribute_Assignment.toString()
        dateService.isValidISODateTime(J.getLiteralValue(response[0], DCTerms.created))
        J.getIri(response[0], CIDOCCRM.P14_carried_out_by) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getIri(response[0], CIDOCCRM.P140_assigned_attribute_to) == annotatedResourceIri
        J.getIri(response[0], CIDOCCRM.P177_assigned_property_of_type) == annotationProperty
        J.getLiteralValue(response[0], CIDOCCRM.P141_assigned) == annotationValue
        Model currentModel = common.getAllTriples()

        // Analytical project refers to new E13
        currentModel.contains(currentModel.createResource(analyticalProject), CIDOCCRM.P9_consists_of, currentModel.createResource(response[0]["@id"].toString()))
    }

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

        common.post('/sherlock/api/analytical-entity', [
                is_referred_to_by: ['http://data-iremus.huma-num/id/note-1',
                                    'http://data-iremus.huma-num/id/note-2',
                                    'http://data-iremus.huma-num/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique',
                e13s: [
                        p141: 'http://data-iremus.huma-num.fr/id/type-cadence',
                        p141_type: 'URI',
                        p177: 'http://www.cidoc-crm.org/cidoc-crm/P2_has_type',
                        document_context: 'http://data-iremus.huma-num/id/ma-partition',
                        analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique'
                ]
        ])
        def beforeDeleteModel = common.getAllTriples()
        def e13CadenceTypeAttribution = beforeDeleteModel.listSubjectsWithProperty(CIDOCCRM.P141_assigned, beforeDeleteModel.createResource("http://data-iremus.huma-num.fr/id/type-cadence")).nextResource()
        def e13AnalyticalEntityCreation = beforeDeleteModel.listSubjectsWithProperty(CIDOCCRM.P140_assigned_attribute_to, beforeDeleteModel.createResource("http://data-iremus.huma-num/id/note-1")).nextResource()

        common.delete("/sherlock/api/e13/${e13CadenceTypeAttribution.toString().split("/").last()}")
        def afterFirstE13DeleteModel = common.getAllTriples()
        common.delete("/sherlock/api/e13/${e13AnalyticalEntityCreation.toString().split("/").last()}?propagate=true")
        def afterDeleteModel = common.getAllTriples()
        then:

        beforeDeleteModel.size() == 24
        afterFirstE13DeleteModel.size() == 15
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

        common.post('/sherlock/api/analytical-entity', [
                is_referred_to_by: ['http://data-iremus.huma-num/id/note-1',
                                    'http://data-iremus.huma-num/id/note-2',
                                    'http://data-iremus.huma-num/id/note-3'
                ],
                document_context: 'http://data-iremus.huma-num/id/ma-partition',
                analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique',
                e13s: [
                        p141: 'http://data-iremus.huma-num.fr/id/type-cadence',
                        p141_type: 'URI',
                        p177: 'http://www.cidoc-crm.org/cidoc-crm/P2_has_type',
                        document_context: 'http://data-iremus.huma-num/id/ma-partition',
                        analytical_project: 'http://data-iremus.huma-num/id/mon-projet-analytique'
                ]
        ])
        def beforeDeleteModel = common.getAllTriples()
        def e13CadenceTypeAttribution = beforeDeleteModel.listSubjectsWithProperty(CIDOCCRM.P141_assigned, beforeDeleteModel.createResource("http://data-iremus.huma-num.fr/id/type-cadence")).nextResource()
        def e13AnalyticalEntityCreation = beforeDeleteModel.listSubjectsWithProperty(CIDOCCRM.P140_assigned_attribute_to, beforeDeleteModel.createResource("http://data-iremus.huma-num/id/note-1")).nextResource()

        common.delete("/sherlock/api/e13/${e13CadenceTypeAttribution.toString().split("/").last()}")
        def afterFirstE13DeleteModel = common.getAllTriples()
        common.delete("/sherlock/api/e13/${e13AnalyticalEntityCreation.toString().split("/").last()}")
        def afterDeleteModel = common.getAllTriples()
        then:

        beforeDeleteModel.size() == 24
        afterFirstE13DeleteModel.size() == 15
        // 4 triples because E28 is not deleted
        afterDeleteModel.size() == 4
    }

}