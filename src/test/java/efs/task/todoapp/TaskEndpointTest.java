package efs.task.todoapp;

import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
public class TaskEndpointTest {
    public static final int CREATED = HttpStatus.CREATED.value();
    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    public static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.value();
    public static final String TODO_APP_PATH = "http://localhost:8080/todo/";
    public static final String TASK_JSON = "{\"description\": \"Kup mleko\", \"due\": \"2021-06-30\"}";
    public static final String USER_JSON = "{\"username\": \"janKowalski\", \"password\": \"am!sK#123\"}";
    private HttpClient httpClient;
    private HttpRequest httpRequest;
    private AuthResponse authResponse;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(USER_JSON))
                .build();
        String responseJson = httpClient.send(httpRequest, ofString()).body();
        authResponse = JsonSerializer.fromJsonToObject(responseJson, AuthResponse.class);
    }

    @Test
    public void shouldReturnCreatedStatusForAddingTask() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .header("auth", authResponse.getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(TASK_JSON))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(CREATED);
    }

    @ParameterizedTest(name = "input {0}")
    @CsvFileSource(resources = {"/badjsontask.csv"})
    public void shouldReturnBadRequestStatusForBadTaskBodyCsv(String input) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .header("auth", authResponse.getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestStatusForEmptyHeader() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .header("auth", "")
                .POST(HttpRequest.BodyPublishers.ofString(TASK_JSON))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestStatusForBadHeader() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TODO_APP_PATH + "task"))
                .header("auth", "xxxx:=xxxx")
                .POST(HttpRequest.BodyPublishers.ofString(TASK_JSON))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(UNAUTHORIZED);
    }
}
