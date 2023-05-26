package efs.task.todoapp;

import efs.task.todoapp.util.TestConstants;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
public class PostTaskEndpointTest {
    private HttpClient httpClient;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON[0]))
                .build();

        httpClient.send(httpRequest, ofString()).body();
    }

    @Test
    @Timeout(1)
    public void shouldReturnCreatedForAddingTask() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        var httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.CREATED);
    }

    @ParameterizedTest(name = "request body = {0}")
    @ValueSource(strings = {"{\"description\": \"buy water\", \"due\": \"date\"}","{}",""})
    @Timeout(1)
    public void shouldReturnBadRequestForBadTaskBody(String body) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_JSON[0])
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // when
        var httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @ParameterizedTest(name = "header = {0}")
    @ValueSource(strings = {"", "lorem:eHh4", "bmFtZQ==:lorem", "bmFtZQ=="})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeaderPost(String header) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", header)
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        var httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    @Timeout(1)
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordPost() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[1])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        var httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }
}
