package fr.cnrs.iremus.sherlock.external.authentication;

import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.annotation.Client;
import org.reactivestreams.Publisher;


@Header(name = "User-Agent", value = "Micronaut")
@Client("https://orcid.org/oauth")
public interface OrcidApiClient {
    @Post("/userinfo")
    Publisher<OrcidUser> get(@Header("Authorization") String Authorization);
}

