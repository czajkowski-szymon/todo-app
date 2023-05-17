package efs.task.todoapp;

import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(ToDoServerExtension.class)
public class UserEndpointTest {
    public static final int CREATED = HttpStatus.CREATED.value();
    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    public static final int CONFLICT = HttpStatus.CONFLICT.value();
    public static final String TODO_APP_PATH = "http://localhost:8080/todo/user";
    public static final String USER_JSON = "{\"username\": \"janKowalski\", \"password\": \"am!sK#123\"}";

    private HttpClient httpClient;

    @BeforeEach
    void setup() {
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    public void shouldReturnCreatedStatusForAddingUser() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH))
                .POST(HttpRequest.BodyPublishers.ofString(USER_JSON))
                .build();

        //when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(CREATED);
    }

    @ParameterizedTest(name = "input {0}")
    @CsvFileSource(resources = {"/badjsonuser.csv"})
    public void shouldReturnBadRequestStatusForBadUserBodyCsv(String input) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH))
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
                .uri(URI.create(TODO_APP_PATH))
                .POST(HttpRequest.BodyPublishers.ofString(USER_JSON))
                .build();

        var httpRequest2 = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH))
                .POST(HttpRequest.BodyPublishers.ofString(USER_JSON))
                .build();

        //when
        httpClient.send(httpRequest1, ofString());

        HttpResponse<String> httpResponse2 = httpClient.send(httpRequest2, ofString());

        // then
        assertThat(httpResponse2.statusCode()).isEqualTo(CONFLICT);
    }
}
