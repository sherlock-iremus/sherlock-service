package fr.cnrs.iremus.sherlock.controller.user;

import fr.cnrs.iremus.sherlock.common.Sherlock;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfig;
import fr.cnrs.iremus.sherlock.pojo.user.config.UserConfigEdit;
import fr.cnrs.iremus.sherlock.service.UserService;
import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.*;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import org.apache.http.HttpException;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.sparql.lang.sparql_11.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller("/api/user/config")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Tag(name = "2. User Configuration")
public class UserConfigController {
    private static Logger logger = LoggerFactory.getLogger(UserConfigController.class);

    @Property(name = "jena")
    protected String jena;

    @Inject
    Sherlock sherlock;

    @Inject
    UserService userService;

    @Put
    @Produces(MediaType.TEXT_PLAIN)
    public MutableHttpResponse<String> edit(@RequestBody(content = {@Content(mediaType = "application/json", schema = @Schema(implementation = UserConfigEdit.class), examples = {@ExampleObject(value = """
                {
                    "emoji": "♫",
                    "color": "b985c7"
                }
            """)})}) @Valid @Body UserConfigEdit body, Authentication authentication) throws HttpException, ParseException {
        logger.info("Setting config { emoji: %s, color: %s } for user : %s".formatted(body.getEmoji(), body.getColor(), authentication.getAttributes().get("uuid")));

        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        Model m = ModelFactory.createDefaultModel();
        Resource authenticatedUser = m.createResource(sherlock.makeIri(authenticatedUserUuid));
        if (userService.getUserByUuid((String) authentication.getAttributes().get("uuid")) == null)
            return HttpResponse.badRequest("No user found. Please reconnect");
        if (body.getEmoji() != null) userService.editEmoji(authenticatedUser, body.getEmoji());
        if (body.getColor() != null) userService.editHexColor(authenticatedUser, body.getColor());
        return HttpResponse.ok("User updated");
    }

    @Get
    @Produces(MediaType.APPLICATION_JSON)
    public MutableHttpResponse<UserConfig> get(Authentication authentication) throws HttpException, ParseException {
        logger.info("Getting config for user : " + authentication.getAttributes().get("uuid"));

        String authenticatedUserUuid = (String) authentication.getAttributes().get("uuid");
        return HttpResponse.ok(userService.getUserConfigByUuid(authenticatedUserUuid));
    }
}
