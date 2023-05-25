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

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.UUID;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

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
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON[1]))
                .build();

        httpClient.send(httpRequest, ofString());
    }

    @Test
    @Timeout(1)
    public void shouldReturnTasks() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestPOST1 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST1, ofString()).body();
        UUIDResponse uuidResponse = JsonSerializer.fromJsonToObject(httpResponsePOST, UUIDResponse.class);

        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(TestConstants.TASK_JSON[0], TaskEntity.class);
        taskEntity.setUuid(uuidResponse.getUuid());


        HttpRequest httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .GET()
                .build();

        // when
        String httpResponseGET = httpClient.send(httpRequestGET, ofString()).body();

        // then
        assertThat(httpResponseGET).isEqualTo(JsonSerializer.fromObjectToJson(Arrays.asList(taskEntity)));
    }

    @ParameterizedTest(name = "header = {0}")
    @CsvFileSource(resources = {"/badheaders.csv"})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeaderGetTasks(String header) throws IOException, InterruptedException {
        // given
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", header)
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordGetTasks() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[1])
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    @Timeout(1)
    public void shouldReturnTaskByID() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        UUIDResponse uuidResponse = JsonSerializer.fromJsonToObject(httpResponsePOST, UUIDResponse.class);

        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(TestConstants.TASK_JSON[0], TaskEntity.class);
        taskEntity.setUuid(uuidResponse.getUuid());

        HttpRequest httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuidResponse.getUuid()))
                .header("auth", TestConstants.USER_AUTH[0])
                .GET()
                .build();

        // when
        String httpResponseGET = httpClient.send(httpRequestGET, ofString()).body();

        // then
        assertThat(httpResponseGET).isEqualTo(JsonSerializer.fromObjectToJson(taskEntity));
    }

    @ParameterizedTest(name = "header = {0}")
    @CsvFileSource(resources = {"/badheaders.csv"})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeaderGetTask(String header) throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        HttpRequest httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", header)
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    @Timeout(1)
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordGetTask() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        HttpRequest httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", TestConstants.USER_AUTH[1])
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    @Timeout(1)
    public void shouldReturnForbiddenForWrongUserGetTask() throws IOException, InterruptedException {
        // given
        addSecondUser();

        HttpRequest httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();

        UUIDResponse uuidResponse = JsonSerializer.fromJsonToObject(httpResponsePOST, UUIDResponse.class);
        String uuid = uuidResponse.getUuid().toString();

        HttpRequest httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", TestConstants.USER_AUTH[1])
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.FORBIDDEN);
    }

    @Test
    @Timeout(1)
    public void shouldReturnNotFoundForNonExistingTaskGet() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + UUID.randomUUID()))
                .header("auth", TestConstants.USER_AUTH[0])
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.NOT_FOUND);
    }
}
