package fr.cnrs.iremus.sherlock.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.cnrs.iremus.sherlock.common.Sherlock;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;

@Controller("/api/")
@Tag(name = "1. Home")
@Secured(SecurityRule.IS_AUTHENTICATED)
public class HomeController {

    @Inject
    Sherlock sherlock;

    @Produces(MediaType.APPLICATION_JSON)
    @Get
    @ApiResponse(description = "Current user uuid", content = @Content(mediaType = "application/json", schema = @Schema(implementation = String.class), examples = @ExampleObject("6ea17744-2345-43ee-8a3e-f3c9770e0340")))
    @ApiResponse(responseCode = "401", description = "User has no valid token")
    public MutableHttpResponse<String> index(@NotNull Authentication authentication) throws JsonProcessingException {
        return HttpResponse.ok(sherlock.objectToJson(authentication.getAttributes().get("uuid")));
    }
}