package efs.task.todoapp;

import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.UUIDResponse;
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
import java.util.UUID;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;

@ExtendWith(ToDoServerExtension.class)
public class PutEndpointTest {
    private HttpClient httpClient;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON[0]))
                .build();

        httpClient.send(httpRequest, ofString());
    }

    @Test
    @Timeout(1)
    public void shouldUpdateTask() throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        var httpResponsePOST = httpClient.send(httpRequestPOST, ofString());
        UUIDResponse uuid = JsonSerializer.fromJsonToObject(httpResponsePOST.body(), UUIDResponse.class);

        var httpRequestPUT = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid.getUuid()))
                .header("auth", TestConstants.USER_AUTH[0])
                .PUT(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[1]))
                .build();

        httpClient.send(httpRequestPUT, ofString());

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid.getUuid()))
                .header("auth", TestConstants.USER_AUTH[0])
                .GET()
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());
        TestConstants.TASK_OBJECT[1].setUuid(uuid.getUuid());

        // then
        assertAll(
                () -> assertEquals(httpResponseGET.body(), JsonSerializer.fromObjectToJson(TestConstants.TASK_OBJECT[1])),
                () -> assertEquals(TestConstants.OK, httpResponseGET.statusCode())
        );
    }

    @ParameterizedTest(name = "header = {0}")
    @ValueSource(strings = {"", "lorem:eHh4", "bmFtZQ==:lorem", "bmFtZQ=="})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeaderPut(String header) throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        var httpResponsePOST = httpClient.send(httpRequestPOST, ofString());
        UUIDResponse uuid = JsonSerializer.fromJsonToObject(httpResponsePOST.body(), UUIDResponse.class);

        var httpRequestPUT = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid.getUuid()))
                .header("auth", header)
                .PUT(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[1]))
                .build();

        // when
        var httpResponsePUT = httpClient.send(httpRequestPUT, ofString());

        // then
        assertThat(httpResponsePUT.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @ParameterizedTest(name = "request body = {0}")
    @ValueSource(strings = {"{\"description\": \"kup mleko\", \"due\": \"date\"}","{}",""})
    @Timeout(1)
    public void shouldReturnBadRequestForBadTaskBodyPut(String body) throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        var httpResponsePOST = httpClient.send(httpRequestPOST, ofString());
        UUIDResponse uuid = JsonSerializer.fromJsonToObject(httpResponsePOST.body(), UUIDResponse.class);

        var httpRequestPUT = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", TestConstants.USER_AUTH[1])
                .PUT(HttpRequest.BodyPublishers.ofString(body))
                .build();

        // when
        var httpResponsePUT = httpClient.send(httpRequestPUT, ofString());

        // then
        assertThat(httpResponsePUT.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @ParameterizedTest(name = "path = {0}")
    @ValueSource(strings = {"task", "task/111"})
    @Timeout(1)
    public void shouldReturnBadRequestForBadPathPut(String path) throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        httpClient.send(httpRequestPOST, ofString());

        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + path))
                .header("auth", TestConstants.USER_AUTH[0])
                .PUT(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[1]))
                .build();

        // when
        var httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    @Timeout(1)
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordPut() throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        var httpResponsePOST = httpClient.send(httpRequestPOST, ofString());
        UUIDResponse uuid = JsonSerializer.fromJsonToObject(httpResponsePOST.body(), UUIDResponse.class);

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid.getUuid()))
                .header("auth", TestConstants.USER_AUTH[1])
                .PUT(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    @Timeout(1)
    public void shouldReturnForbiddenForWrongUserPut() throws IOException, InterruptedException {
        // given
        var httpRequestSecondUser = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON[1]))
                .build();

        httpClient.send(httpRequestSecondUser, ofString());

        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        var httpResponsePOST = httpClient.send(httpRequestPOST, ofString());
        UUIDResponse uuid = JsonSerializer.fromJsonToObject(httpResponsePOST.body(), UUIDResponse.class);

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid.getUuid()))
                .header("auth", TestConstants.USER_AUTH[1])
                .PUT(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[1]))
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.FORBIDDEN);
    }

    @Test
    @Timeout(1)
    public void shouldReturnNotFoundForNonExistingTaskPut() throws IOException, InterruptedException {
        // given
        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + UUID.randomUUID()))
                .header("auth", TestConstants.USER_AUTH[0])
                .PUT(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.NOT_FOUND);
    }
}
