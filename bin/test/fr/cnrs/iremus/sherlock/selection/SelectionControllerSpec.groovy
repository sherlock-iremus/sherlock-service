package fr.cnrs.iremus.sherlock.selection

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.pojo.selection.SelectionCreate
import fr.cnrs.iremus.sherlock.service.DateService
import fr.cnrs.iremus.sherlock.service.SelectionService
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.client.exceptions.HttpClientResponseException
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.Resource
import org.apache.jena.vocabulary.DCTerms
import spock.lang.Specification
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import io.micronaut.rxjava2.http.client.RxHttpClient
import jakarta.inject.Inject
import fr.cnrs.iremus.sherlock.common.Sherlock

@MicronautTest
class SelectionControllerSpec extends Specification {

    @Inject
    @Client('/')
    RxHttpClient client

    @Inject
    DateService dateService

    @Inject
    Common common

    @Inject
    Sherlock sherlock

    @Inject
    SelectionService selectionService

    void 'test post selection creates triples'() {
        when:
        common.eraseall()
        String documentContext1Iri = sherlock.makeIri()
        String documentContext2Iri = sherlock.makeIri()
        String child1Iri = sherlock.makeIri()
        String child2Iri = sherlock.makeIri()

        def response = common.post('/sherlock/api/selection', [
                'document_contexts': [documentContext1Iri, documentContext2Iri],
                'children': [child1Iri, child2Iri],
        ])

        then:
        response[0]["@type"][0] == CIDOCCRM.E28_Conceptual_Object.URI
        response[0][CIDOCCRM.P106_is_composed_of.URI].find(child -> child["@id"] == child1Iri)
        response[0][CIDOCCRM.P106_is_composed_of.URI].find(child -> child["@id"] == child2Iri)
        response[0][Sherlock.has_document_context.URI].find(child -> child["@id"] == documentContext1Iri)
        response[0][Sherlock.has_document_context.URI].find(child -> child["@id"] == documentContext2Iri)
        response[0][CIDOCCRM.P2_has_type.URI].find(child -> child["@id"] == SelectionCreate.e55SelectionTypeIri)
        dateService.isValidISODateTime(J.getLiteralValue(response[0], DCTerms.created))
        J.getIri(response[0], DCTerms.creator) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
    }

    void 'test patch selection does fail if selection does not exist'() {
        when:
        common.eraseall()
        String child1Iri = sherlock.makeIri()
        String child2Iri = sherlock.makeIri()
        String document_contextsIri = sherlock.makeIri()

        def response = common.patch('/sherlock/api/selection/mySelectionWhichDoesNotExist', [
                'children': [child1Iri, child2Iri],
                'document_contexts': document_contextsIri
        ])

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 404
    }

    void 'test patch selection does return new child and not previous one'() {
        when:
        common.eraseall()
        String child1Iri = sherlock.makeIri()
        String child2Iri = sherlock.makeIri()
        String document_contextsIri = sherlock.makeIri()

        def postResponse = common.post('/sherlock/api/selection/', [
                'children': [child1Iri],
                'document_contexts': document_contextsIri
        ])

        def selectionIri = postResponse[0]["@id"] as String
        def selectionUuid = selectionIri.split("/").last()

        def response = common.patch("/sherlock/api/selection/${selectionUuid}", [
                'children': [child2Iri],
                'document_contexts': document_contextsIri
        ])

        then:
        !response[0][CIDOCCRM.P106_is_composed_of.URI].find(child -> child["@id"] == child1Iri)
        response[0][CIDOCCRM.P106_is_composed_of.URI].find(child -> child["@id"] == child2Iri)
    }

    void 'test delete selection erase it'() {
        when:
        common.eraseall()
        Model m = ModelFactory.createDefaultModel()
        String child1Iri = sherlock.makeIri()
        String document_contextsIri = sherlock.makeIri()

        def postResponse = common.post('/sherlock/api/selection/', [
                'children': [child1Iri],
                'document_contexts': document_contextsIri
        ])

        def selectionIri = postResponse[0]["@id"] as String
        def selectionUuid = selectionIri.split("/").last()
        Resource selection = m.createResource(selectionIri)

        common.delete("/sherlock/api/selection/${selectionUuid}")

        then:
        Model currentModel = selectionService.getSelectionTriplesByResource(selection)
        currentModel.empty
    }

    void 'test deleting not existing selection returns 404'() {
        when:
        common.eraseall()
        common.delete("/sherlock/api/selection/my-not-existing-selection")

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 404
    }

}
