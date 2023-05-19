package efs.task.todoapp;

import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.util.TestConstants;
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
import java.util.UUID;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
public class PostTaskEndpointTest {
    private HttpClient httpClient;
    private AuthResponse authResponse;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON_1))
                .build();

        String responseJson = httpClient.send(httpRequest, ofString()).body();
        authResponse = JsonSerializer.fromJsonToObject(responseJson, AuthResponse.class);
    }

    @Test
    public void shouldReturnCreatedForAddingTask() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", authResponse.getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.CREATED);
    }

    @ParameterizedTest(name = "input {0}")
    @CsvFileSource(resources = {"/badjsontask.csv"})
    public void shouldReturnBadRequestForBadTaskBodyCsv(String input) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", authResponse.getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    public void shouldReturnBadRequestForEmptyHeaderPost() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", "")
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    public void shouldReturnUnauthorizedForBadHeaderPost() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", "xxxx:=xxxx")
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }
}
