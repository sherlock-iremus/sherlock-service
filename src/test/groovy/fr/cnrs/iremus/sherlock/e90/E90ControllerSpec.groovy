package fr.cnrs.iremus.sherlock.e90

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
class E90ControllerSpec extends Specification {
    @Inject
    Common common
    @Inject
    Sherlock sherlock

    void 'test controller chose the most accurate rdf:type inherited from e90 parent'() {
        when:
        common.eraseall()
        String annotatedResourceIri = "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"
        String annotationProperty = "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique"
        String documentContext = "http://data-iremus.huma-num/id/ma-partition"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def responsePostE13 = common.post('/sherlock/api/e13', [
                "p140"              : [annotatedResourceIri],
                "p177"              : annotationProperty,
                "p141_type" : "new resource",
                "new_p141"              : [
                        rdf_type: ["crm:E36_Visual_Item", "crm:E90_Symbolic_Object"],
                        p2_type: ["http://data-iremus.huma-num.fr/id/element-visuel"],
                ],
                "document_context"  : documentContext,
                "analytical_project": analyticalProject
        ])

        def parent_e90 = J.getOneByType(responsePostE13, CIDOCCRM.E36_Visual_Item)
        def responsePostFragment = common.post('/sherlock/api/e90/fragment', [
                "parent"              : parent_e90["@id"],
                "p2_type"         : ["http://data-iremus.huma-num.fr/id/fragment-d-image", "http://data-iremus.huma-num.fr/id/image-mercure-galant"],
        ])

        then:

        def e90Fragment = J.getOneByType(responsePostFragment, CIDOCCRM.E36_Visual_Item)

        J.getIri(e90Fragment, DCTerms.creator) == sherlock.makeIri("4b15a57d-8cae-43c5-8096-187b58d29327")
        J.getLiteralValue(e90Fragment, DCTerms.created)
        J.getIri(e90Fragment, CIDOCCRM.P106i_forms_part_of) == parent_e90["@id"]
        e90Fragment[CIDOCCRM.P2_has_type.URI]["@id"].contains("http://data-iremus.huma-num.fr/id/fragment-d-image")
        e90Fragment[CIDOCCRM.P2_has_type.URI]["@id"].contains("http://data-iremus.huma-num.fr/id/image-mercure-galant")
    }

    void 'test creation does not work if parent resource has no type inheriting from E90'() {
        when:
        common.eraseall()

        common.post('/sherlock/api/e90/fragment', [
                "parent"              : "http://data-iremus.huma-num/id/ma-resource-non-typee",
                "p2_type"         : ["http://data-iremus.huma-num/id/fragment-d-image", "http://data-iremus.huma-num/id/image-mercure-galant"],
        ])

        then:
        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.message == "Parent resource has no rdf:type matching E90"
    }
}