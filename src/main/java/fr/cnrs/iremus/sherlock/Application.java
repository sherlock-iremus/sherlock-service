package fr.cnrs.iremus.sherlock;

import io.micronaut.runtime.Micronaut;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;


@OpenAPIDefinition(
        info = @Info(
                title = "SHERLOCK API",
                version = "0.0",
                description = "SHERLOCK API",
                license = @License(name = "Apache 2.0", url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(url = "http://iremus.cnrs.fr", name = "Thomas Bottini", email = "thomas.bottini@cnrs.fr")
        ),
        security = @SecurityRequirement(name = "orcid")
)
@SecurityScheme(
        name = "orcid",
        in = SecuritySchemeIn.COOKIE,
        description = "<a href='/sherlock/login?redirect-uri=/sherlock/rapidoc'>oauth2</a>",
        paramName = "jwt",
        type = SecuritySchemeType.OPENIDCONNECT
)
public class Application {
    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
