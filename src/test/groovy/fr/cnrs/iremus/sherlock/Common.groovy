package fr.cnrs.iremus.sherlock


import fr.cnrs.iremus.sherlock.common.Sherlock
import groovy.json.JsonSlurper
import io.micronaut.context.annotation.Property
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.client.annotation.Client
import io.micronaut.rxjava3.http.client.Rx3HttpClient
import io.micronaut.security.authentication.UsernamePasswordCredentials
import io.micronaut.security.token.render.BearerAccessRefreshToken
import jakarta.inject.Inject
import org.apache.jena.arq.querybuilder.ConstructBuilder
import org.apache.jena.atlas.web.HttpException
import org.apache.jena.query.Query
import org.apache.jena.query.QueryExecution
import org.apache.jena.rdf.model.Model
import org.apache.jena.rdf.model.ModelFactory
import org.apache.jena.rdf.model.RDFNode
import org.apache.jena.rdf.model.Resource
import org.apache.jena.rdfconnection.RDFConnectionFuseki
import org.apache.jena.rdfconnection.RDFConnectionRemoteBuilder

class Common {

    @Inject
    @Client("/")
    Rx3HttpClient client

    @Inject
    Sherlock sherlock
    @Property(name = "jena")
    protected String jena

    public static final String tonalitiesGraph = "http://data-iremus.huma-num.fr/graph/tonalities-contributions"
    @Deprecated
    String getAccessToken(client) {
        UsernamePasswordCredentials creds = new UsernamePasswordCredentials('sherlock', 'kcolrehs')
        HttpRequest request = HttpRequest.POST('/sherlock/api/login', creds)
        HttpResponse<BearerAccessRefreshToken> rsp = client.toBlocking().exchange(request, BearerAccessRefreshToken)
        BearerAccessRefreshToken bearerAccessRefreshToken = rsp.body()
        String accessToken = bearerAccessRefreshToken.accessToken
        return accessToken
    }

    @Deprecated
    HttpRequest makePostRequestWithAuthorization(client, String uri, requestBody) {
        HttpRequest requestWithAuthorization = HttpRequest.POST(uri, requestBody)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(getAccessToken(client))
        return requestWithAuthorization
    }

    @Deprecated
    HttpRequest makePutRequestWithAuthorization(client, String uri, requestBody) {
        HttpRequest requestWithAuthorization = HttpRequest.PUT(uri, requestBody)
                .accept(MediaType.APPLICATION_JSON_TYPE)
                .bearerAuth(getAccessToken(client))
        return requestWithAuthorization
    }

    static Object parse(String json) {
        def jsonSlurper = new JsonSlurper()
        def object = jsonSlurper.parseText(json)
        return object
    }

    Object post(String route, Map body) {
        HttpRequest request = HttpRequest
                .POST(route, body)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        String response = client.toBlocking().retrieve(request)
        return parse(response)
    }

    Object patch(String route, Map body) {
        HttpRequest request = HttpRequest
                .PATCH(route, body)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        String response = client.toBlocking().retrieve(request)
        return parse(response)
    }

    Object delete(String route) {
        HttpRequest request = HttpRequest
                .DELETE(route)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        String response = client.toBlocking().retrieve(request)
        return parse(response)
    }

    Object put(String route, Map body) {
        HttpRequest request = HttpRequest
                .PUT(route, body)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        String response = client.toBlocking().retrieve(request)
        return parse(response)
    }

    Object get(String route) {
        HttpRequest request = HttpRequest
                .GET(route)
                .accept(MediaType.APPLICATION_JSON_TYPE)
        String response = client.toBlocking().retrieve(request)
        return parse(response)
    }

    void eraseall() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination('http://localhost:3030/test')
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            try {
                conn.delete(sherlock.getGraph().toString())
            } catch(Exception e) {}
            try {
                conn.delete(sherlock.getUserGraph().toString())
            } catch (Exception e) {}
            try {
                conn.delete(tonalitiesGraph)
            } catch (Exception e) {}
        } catch (HttpException e) {}
    }

    void addTripleToDataset(Resource s, org.apache.jena.rdf.model.Property p, RDFNode o) {
        Model m = ModelFactory.createDefaultModel()
        m.add(s, p, o)
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination('http://localhost:3030/test')
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            conn.load(sherlock.getGraph().toString(), m)
        } catch (HttpException e) {
        }
    }

    void addTripleToDataset(Resource s, org.apache.jena.rdf.model.Property p, RDFNode o, String graph) {
        Model m = ModelFactory.createDefaultModel()
        m.add(s, p, o)
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination('http://localhost:3030/test')
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {
            conn.load(graph, m)
        } catch (HttpException e) {
        }
    }

    Model getAllTriples() {
        RDFConnectionRemoteBuilder builder = RDFConnectionFuseki.create().destination(jena)
        try (RDFConnectionFuseki conn = (RDFConnectionFuseki) builder.build()) {

            ConstructBuilder cb = new ConstructBuilder()
                    .addConstruct("?s", "?p", "?o")
                    .addGraph("?g", "?s", "?p", "?o")
            Query q = cb.build()
            QueryExecution qe = conn.query(q)
            return qe.execConstruct()
        }
    }

    static Resource createResource(String s) {
        Model m = ModelFactory.createDefaultModel()
        return m.createResource(s);
    }

    static org.apache.jena.rdf.model.Property createProperty(String s) {
        Model m = ModelFactory.createDefaultModel()
        return m.createProperty(s);
    }
}
