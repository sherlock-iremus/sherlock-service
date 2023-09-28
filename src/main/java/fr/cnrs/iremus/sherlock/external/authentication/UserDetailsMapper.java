package fr.cnrs.iremus.sherlock.external.authentication;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.service.UserService;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.micronaut.security.oauth2.endpoint.authorization.state.State;
import io.micronaut.security.oauth2.endpoint.token.response.OauthAuthenticationMapper;
import io.micronaut.security.oauth2.endpoint.token.response.TokenResponse;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;

import java.util.Map;

@Named("orcid")
@Singleton
public class UserDetailsMapper implements OauthAuthenticationMapper {
    private static Logger logger = LoggerFactory.getLogger(UserDetailsMapper.class);

    private final OrcidApiClient orcidApiClient;
    @Inject
    UserService userService;
    @Inject
    Sherlock sherlock;


    public UserDetailsMapper(OrcidApiClient orcidApiClient) {
        this.orcidApiClient = orcidApiClient;
    }

    public Publisher<AuthenticationResponse> createAuthenticationResponse(TokenResponse tokenResponse, State state) {
        try {

            logger.info("[ORCID API] Received token");
            logger.info("[ORCID API] /GET user ");
            Publisher<OrcidUser> response = orcidApiClient.get("bearer " + tokenResponse.getAccessToken());
            OrcidUser user = Flux.from(response).blockFirst();
            logger.info("[ORCID API] Received user's identity");
            String userUuid = sherlock.getUuidFromSherlockUri(userService.createUserIfNotExists(user.getSub()));
            logger.info("[DATA] created or retrieved user");
            return Publishers.just(AuthenticationResponse.success(
                    user.getSub(),
                    Map.ofEntries(
                            Map.entry("orcid", user.getSub()),
                            Map.entry("uuid", userUuid)
                    )));
        } catch (Exception exception) {
            return Publishers.just(AuthenticationResponse.failure(exception.getMessage()));
        }
    }
}