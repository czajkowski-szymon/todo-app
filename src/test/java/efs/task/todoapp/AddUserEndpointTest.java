package efs.task.todoapp;

import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(ToDoServerExtension.class)
public class AddUserEndpointTest {
    public static final int CREATED = 201;
    public static final int BAD_REQUEST = 400;
    public static final int CONFLICT = 409;
    public static final String TODO_APP_PATH = "http://localhost:8080/todo/";
    public static final String USER_JSON = "{\"username\": \"janKowalski\", \"password\": \"haslomaslo\"}";
    public static final String BAD_JSON = "{\"name\": \"janKowalski\", \"word\": \"haslomaslo\"}";

    private HttpClient httpClient;

    @BeforeEach
    void setUp() {
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    public void shouldReturnCreatedStatusForAddingUser() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(USER_JSON))
                .build();

        //when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(CREATED);
    }

    @ParameterizedTest(name = "input {0}")
    @ValueSource(strings = {"{}", "{  }", BAD_JSON})
    public void shouldReturnBadRequestStatusForBadJson(String input) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();

        //when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnConflictForAlreadyAddedUser() throws IOException, InterruptedException {
        // given
        var httpRequest1 = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(USER_JSON))
                .build();

        var httpRequest2 = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(USER_JSON))
                .build();

        //when
        httpClient.send(httpRequest1, ofString());

        HttpResponse<String> httpResponse2 = httpClient.send(httpRequest2, ofString());

        // then
        assertThat(httpResponse2.statusCode()).isEqualTo(CONFLICT);
    }
}
