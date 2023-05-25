package efs.task.todoapp;

import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.util.TestConstants;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
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

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON[0]))
                .build();

        httpClient.send(httpRequest, ofString()).body();
    }

    @Test
    @Timeout(1)
    public void shouldReturnCreatedForAddingTask() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.CREATED);
    }

    @ParameterizedTest(name = "request body = {0}")
    @CsvFileSource(resources = {"/badjsontask.csv"})
    @Timeout(1)
    public void shouldReturnBadRequestForBadTaskBodyCsv(String body) throws IOException, InterruptedException {
        // given
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_JSON[0])
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @ParameterizedTest(name = "header = {0}")
    @CsvFileSource(resources = {"/badheaders.csv"})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeadersPostCsv(String header) throws IOException, InterruptedException {
        // given
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", header)
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    @Timeout(1)
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordPost() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[1])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }
}
