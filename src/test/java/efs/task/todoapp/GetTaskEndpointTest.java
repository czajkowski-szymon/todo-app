package efs.task.todoapp;

import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UUIDResponse;
import efs.task.todoapp.util.TestConstants;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.UUID;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(ToDoServerExtension.class)
public class GetTaskEndpointTest {
    private HttpClient httpClient;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON[0]))
                .build();

        httpClient.send(httpRequest, ofString());
    }

    private void addSecondUser() throws IOException, InterruptedException {
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON[1]))
                .build();

        httpClient.send(httpRequest, ofString());
    }

    @Test
    @Timeout(1)
    public void shouldReturnTasksForUser() throws IOException, InterruptedException {
        // given
        var httpRequestPOST1 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        var httpRequestPOST2 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[1]))
                .build();

        String httpResponsePOST1 = httpClient.send(httpRequestPOST1, ofString()).body();
        UUIDResponse uuid1 = JsonSerializer.fromJsonToObject(httpResponsePOST1, UUIDResponse.class);
        String httpResponsePOST2 = httpClient.send(httpRequestPOST2, ofString()).body();
        UUIDResponse uuid2 = JsonSerializer.fromJsonToObject(httpResponsePOST2, UUIDResponse.class);
        TestConstants.TASK_OBJECT[0].setUuid(uuid1.getUuid());
        TestConstants.TASK_OBJECT[1].setUuid(uuid2.getUuid());

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .GET()
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());
        TaskEntity[] taskEntities = JsonSerializer.fromJsonToObject(httpResponseGET.body(), TaskEntity[].class);
        boolean option1 = taskEntities[0].equals(TestConstants.TASK_OBJECT[0]) &&
                taskEntities[1].equals(TestConstants.TASK_OBJECT[1]);
        boolean option2 = taskEntities[1].equals(TestConstants.TASK_OBJECT[0]) &&
                taskEntities[0].equals(TestConstants.TASK_OBJECT[1]);

        // then
        assertAll(
                () -> assertTrue(option1 || option2),
                () -> assertEquals(TestConstants.OK, httpResponseGET.statusCode())
        );
    }

    @ParameterizedTest(name = "header = {0}")
    @ValueSource(strings = {"", "lorem:eHh4", "bmFtZQ==:lorem", "bmFtZQ=="})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeaderGetTasks(String header) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", header)
                .GET()
                .build();

        // when
        var httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordGetTasks() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[1])
                .GET()
                .build();

        // when
        var httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    @Timeout(1)
    public void shouldReturnTask() throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        UUIDResponse uuid = JsonSerializer.fromJsonToObject(httpResponsePOST, UUIDResponse.class);
        TestConstants.TASK_OBJECT[0].setUuid(uuid.getUuid());

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid.getUuid()))
                .header("auth", TestConstants.USER_AUTH[0])
                .GET()
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.body()).isEqualTo(JsonSerializer.fromObjectToJson(TestConstants.TASK_OBJECT[0]));
    }

    @ParameterizedTest(name = "header = {0}")
    @ValueSource(strings = {"", "lorem:eHh4", "bmFtZQ==:lorem", "bmFtZQ=="})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeaderGetTask(String header) throws IOException, InterruptedException {
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
                .header("auth", header)
                .GET()
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    @Timeout(1)
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordGetTask() throws IOException, InterruptedException {
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
                .GET()
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    @Timeout(1)
    public void shouldReturnForbiddenForWrongUserGetTask() throws IOException, InterruptedException {
        // given
        addSecondUser();

        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();

        UUIDResponse uuidResponse = JsonSerializer.fromJsonToObject(httpResponsePOST, UUIDResponse.class);
        String uuid = uuidResponse.getUuid().toString();

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", TestConstants.USER_AUTH[1])
                .GET()
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.FORBIDDEN);
    }

    @Test
    @Timeout(1)
    public void shouldReturnNotFoundForNonExistingTaskGet() throws IOException, InterruptedException {
        // given
        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + UUID.randomUUID()))
                .header("auth", TestConstants.USER_AUTH[0])
                .GET()
                .build();

        // when
        var httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.NOT_FOUND);
    }
}
