package fr.cnrs.iremus.sherlock.e90

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.J
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class E90DeletionControllerSpec extends Specification {
    @Inject
    Common common

    void 'test deletion works'() {
        when:
        common.eraseall()
        String annotatedResourceIri = "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"
        String annotationProperty = "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique"
        String documentContext = "http://data-iremus.huma-num/id/ma-partition"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def responsePostE13 = common.post('/sherlock/api/e13', [
                "p140"              : [annotatedResourceIri],
                "p177"              : annotationProperty,
                "p141_type" : "NEW_RESOURCE",
                "new_p141"              : [
                        rdf_type: ["crm:E36_Visual_Item", "crm:E90_Symbolic_Object"],
                        p2_type: ["http://data-iremus.huma-num.fr/id/element-visuel"],
                ],
                "document_context"  : documentContext,
                "analytical_project": analyticalProject
        ])

        def modelBeforeFragment = common.getAllTriples()
        def parent_e90 = J.getOneByType(responsePostE13, CIDOCCRM.E36_Visual_Item)

        def responsePostFragment = common.post('/sherlock/api/e90/fragment', [
                "parent"              : parent_e90["@id"],
                "p2_type"         : ["http://data-iremus.huma-num.fr/id/fragment-d-image", "http://data-iremus.huma-num.fr/id/image-mercure-galant"],
        ])

        def e90Fragment = J.getOneByType(responsePostFragment, CIDOCCRM.E36_Visual_Item)
        common.delete("/sherlock/api/e90/fragment/${e90Fragment["@id"].split("/").last()}")
        def modelAfterDeletion = common.getAllTriples()

        then:

        modelBeforeFragment.size() == modelAfterDeletion.size()
    }

    void "test user cannot delete other user's fragment"() {
        when:
        common.eraseall()
        String annotatedResourceIri = "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"
        String annotationProperty = "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique"
        String documentContext = "http://data-iremus.huma-num/id/ma-partition"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def responsePostE13 = common.post('/sherlock/api/e13', [
                "p140"              : [annotatedResourceIri],
                "p177"              : annotationProperty,
                "p141_type" : "NEW_RESOURCE",
                "new_p141"              : [
                        rdf_type: ["crm:E36_Visual_Item", "crm:E90_Symbolic_Object"],
                        p2_type: ["http://data-iremus.huma-num.fr/id/element-visuel"],
                ],
                "document_context"  : documentContext,
                "analytical_project": analyticalProject
        ])

        def parent_e90 = J.getOneByType(responsePostE13, CIDOCCRM.E36_Visual_Item)

        def responsePostFragment = common.post('/sherlock/api/e90/fragment?fake-user=true', [
                "parent"              : parent_e90["@id"],
                "p2_type"         : ["http://data-iremus.huma-num.fr/id/fragment-d-image", "http://data-iremus.huma-num.fr/id/image-mercure-galant"],
        ])

        def e90Fragment = J.getOneByType(responsePostFragment, CIDOCCRM.E36_Visual_Item)
        common.delete("/sherlock/api/e90/fragment/${e90Fragment["@id"].split("/").last()}")

        then:

        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.message == "This E90 belongs to other users."
    }

    void "test user cannot delete a fragment used by an other resource"() {
        when:
        common.eraseall()
        String annotatedResourceIri = "http://data-iremus.huma-num/id/e13-assignant-le-type-cadence"
        String annotationProperty = "http://data-iremus.huma-num/id/commentaire-sur-entite-analytique"
        String documentContext = "http://data-iremus.huma-num/id/ma-partition"
        String analyticalProject = "http://data-iremus.huma-num/id/mon-projet-analytique"

        def responsePostE13 = common.post('/sherlock/api/e13', [
                "p140"              : [annotatedResourceIri],
                "p177"              : annotationProperty,
                "p141_type" : "NEW_RESOURCE",
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

        def e90Fragment = J.getOneByType(responsePostFragment, CIDOCCRM.E36_Visual_Item)

        def responsePostFragmentFragment = common.post('/sherlock/api/e90/fragment', [
                "parent"              : e90Fragment["@id"],
                "p2_type"         : ["http://data-iremus.huma-num.fr/id/fragment-d-image", "http://data-iremus.huma-num.fr/id/image-mercure-galant"],
        ])

        common.delete("/sherlock/api/e90/fragment/${e90Fragment["@id"].split("/").last()}")

        then:

        HttpClientResponseException e = thrown()
        e.getStatus().getCode() == 403
        e.message == "Please delete entities which depends on the this E90 before deleting it."
    }
}
