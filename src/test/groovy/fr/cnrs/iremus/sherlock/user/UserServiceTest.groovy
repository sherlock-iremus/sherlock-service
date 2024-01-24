package fr.cnrs.iremus.sherlock.user

import fr.cnrs.iremus.sherlock.Common
import fr.cnrs.iremus.sherlock.common.CIDOCCRM
import fr.cnrs.iremus.sherlock.common.Sherlock
import fr.cnrs.iremus.sherlock.external.authentication.OrcidUser
import fr.cnrs.iremus.sherlock.service.UserService
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class UserServiceTest extends Specification {
    @Inject
    Common common
    @Inject
    UserService userService

    void 'test creatingUserIfNotExists works with one given-name and simple family name'() {
        when:
        OrcidUser orcidUser = new OrcidUser()
        orcidUser.setFamily_name("Holmes")
        orcidUser.setGiven_name("Sherlock")
        orcidUser.setSub("my-orcid")
        common.eraseall()
        userService.createUserIfNotExists(orcidUser)
        def model = common.getAllTriples()

        then:
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "Sherlock Holmes")
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "SH")
    }

    void 'test creatingUserIfNotExists works with no given-name'() {
        when:
        common.eraseall()
        OrcidUser orcidUser = new OrcidUser()
        orcidUser.setFamily_name("Holmes")
        orcidUser.setSub("my-orcid")
        userService.createUserIfNotExists(orcidUser)
        def model = common.getAllTriples()

        then:
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "Holmes")
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "H")
    }

    void 'test creatingUserIfNotExists works with no family-name'() {
        when:
        common.eraseall()
        OrcidUser orcidUser = new OrcidUser()
        orcidUser.setGiven_name("Sherlock")
        orcidUser.setSub("my-orcid")
        userService.createUserIfNotExists(orcidUser)
        def model = common.getAllTriples()

        then:
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "Sherlock")
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "S")
    }

    void 'test creatingUserIfNotExists works with multiple given-names'() {
        when:
        common.eraseall()
        OrcidUser orcidUser = new OrcidUser()
        orcidUser.setGiven_name("Sherlock Scott")
        orcidUser.setFamily_name("Holmes")
        orcidUser.setSub("my-orcid")
        userService.createUserIfNotExists(orcidUser)
        def model = common.getAllTriples()

        then:
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "Sherlock Scott Holmes")
        model.contains(null, CIDOCCRM.P190_has_symbolic_content, "SH")
    }

}