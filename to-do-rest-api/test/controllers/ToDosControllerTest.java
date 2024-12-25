package controllers;

import com.fasterxml.jackson.databind.JsonNode;
import helpers.RandomStringGenerator;
import org.bson.types.ObjectId;
import org.junit.Before;
import org.junit.Test;
import play.libs.Json;
import play.mvc.Http;
import play.mvc.Result;
import play.test.WithApplication;

import static org.junit.Assert.assertEquals;
import static play.mvc.Http.Status.OK;
import static play.mvc.Http.Status.NOT_FOUND;
import static play.test.Helpers.*;

public class ToDosControllerTest extends WithApplication {
    private String token;
    private String userId = new ObjectId().toString();


    @Before
    public void before() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/generateSignedTokenForTest/" + userId);

        Result result = route(app, request);
        JsonNode jsonResponse = Json.parse(contentAsString(result));
        this.token = jsonResponse.get("accessToken").asText();
    }

    @Test
    public void testListToDos() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/todos")
                .header("Authorization", "Bearer " + token);

        Result result = route(app, request);

        assertEquals(OK, result.status());
    }

    @Test
    public void testListToDosByUserForRandomUser() {
        Http.RequestBuilder request = new Http.RequestBuilder()
                .method(GET)
                .uri("/todos/user/" + RandomStringGenerator.generateRandomString(10))
                .header("Authorization", "Bearer " + token);

        Result result = route(app, request);

        assertEquals(NOT_FOUND, result.status());
    }
}
