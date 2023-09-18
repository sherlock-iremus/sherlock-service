package fr.cnrs.iremus.sherlock.e13

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.ValidateUUID
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.service.DateService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.apache.jena.rdf.model.Model
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification

@MicronautTest
class SimpleE13ControllerSpec extends Specification {
    @Inject
    Common common
    @Inject
    DateService dateService
    @Inject
    Sherlock sherlock

    void 'test it works with literal 141'() {
        when:
        common.eraseall()

        String annotatedResourceIri = "http://data-iremus.huma-num/id/e36-estampe"
        String annotationProperty = "http://data-iremus.huma-num/id/titre-sur-l-image"
        String annotationValue = "Et Nostris Prævalet Alis. Iam Supra Sublimia"
        String documentContext = "http://data-iremus.huma-num/id/e36-estampe"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def response = common.post('/sherlock/api/e13', [
                "p140"              : [annotatedResourceIri],
                "p177"              : annotationProperty,
                "p141"              : annotationValue,
                "p141_type"         : "LITERAL",
                "document_context"  : documentContext,
                "analytical_project": analyticalProject
        ])

        then:

        def e13 = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)
        e13["@id"].startsWith(sherlock.getResourcePrefix())
        ValidateUUID.isValid(e13["@id"].split("/").last())
        sherlock.resolvePrefix(e13["@type"]) == CIDOCCRM.E13_Attribute_Assignment.toString()
        dateService.isValidISODateTime(J.getLiteralValue(e13, DCTerms.created))

        J.getIri(e13, CIDOCCRM.P14_carried_out_by) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getIri(e13, CIDOCCRM.P140_assigned_attribute_to) == annotatedResourceIri
        J.getIri(e13, CIDOCCRM.P177_assigned_property_of_type) == annotationProperty
        J.getIri(e13, Sherlock.has_document_context) == documentContext
        J.getLiteralValue(e13, CIDOCCRM.P141_assigned) == annotationValue
        Model currentModel = common.getAllTriples()

        // Analytical project refers to new E13
        currentModel.contains(currentModel.createResource(analyticalProject), CIDOCCRM.P9_consists_of, currentModel.createResource(e13["@id"].toString()))
    }

    void 'test it works with uri 141'() {
        when:
        common.eraseall()

        String annotatedResourceIri = "http://data-iremus.huma-num/id/e36-estampe"
        String annotationProperty = "http://data-iremus.huma-num/id/indexation-de-thematique"
        String annotationValue = "http://data-iremus.huma-num/id/thematique-religion"
        String documentContext = "http://data-iremus.huma-num/id/e36-estampe"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def response = common.post('/sherlock/api/e13', [
                "p140"              : [annotatedResourceIri],
                "p177"              : annotationProperty,
                "p141"              : annotationValue,
                "p141_type"         : "URI",
                "document_context"  : documentContext,
                "analytical_project": analyticalProject
        ])

        then:

        def e13 = J.getOneByType(response, CIDOCCRM.E13_Attribute_Assignment)
        e13["@id"].startsWith(sherlock.getResourcePrefix())
        ValidateUUID.isValid(e13["@id"].split("/").last())
        sherlock.resolvePrefix(e13["@type"]) == CIDOCCRM.E13_Attribute_Assignment.toString()
        dateService.isValidISODateTime(J.getLiteralValue(e13, DCTerms.created))

        J.getIri(e13, CIDOCCRM.P14_carried_out_by) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getIri(e13, CIDOCCRM.P140_assigned_attribute_to) == annotatedResourceIri
        J.getIri(e13, CIDOCCRM.P177_assigned_property_of_type) == annotationProperty
        J.getIri(e13, Sherlock.has_document_context) == documentContext
        J.getIri(e13, CIDOCCRM.P141_assigned) == annotationValue
        Model currentModel = common.getAllTriples()

        // Analytical project refers to new E13
        currentModel.contains(currentModel.createResource(analyticalProject), CIDOCCRM.P9_consists_of, currentModel.createResource(e13["@id"].toString()))
    }
}