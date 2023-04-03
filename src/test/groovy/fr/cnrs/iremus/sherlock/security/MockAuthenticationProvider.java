package fr.cnrs.iremus.sherlock.security;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.AuthenticationProvider;
import io.micronaut.security.authentication.AuthenticationRequest;
import io.micronaut.security.authentication.AuthenticationResponse;
import io.reactivex.Flowable;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.Map;
import java.util.Objects;

@Singleton
public class MockAuthenticationProvider implements AuthenticationProvider {

    @Override
    public Publisher<AuthenticationResponse> authenticate(HttpRequest<?> httpRequest, AuthenticationRequest<?, ?> authenticationRequest) {
        boolean shouldFakeUser = Objects.equals(httpRequest.getParameters().get("fake-user"), "true");
        Map<String, Object> attributes = Map.of("uuid", shouldFakeUser ? "0bd155cc-d23a-11ed-afa1-0242ac120002" : "4b15a57d-8cae-43c5-8096-187b58d29327");
        return Flowable.just(AuthenticationResponse.success("sherlock", attributes));
    }
}