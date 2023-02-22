package fr.cnrs.iremus.sherlock.e13

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.ValidateUUID
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava2.http.client.RxHttpClient
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

@MicronautTest
class E13ControllerSpec extends Specification {
    @Inject
    @Client("/")
    RxHttpClient client
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
}