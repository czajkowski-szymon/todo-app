package efs.task.todoapp;

import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.util.TestConstants;
import efs.task.todoapp.util.ToDoServerExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.UUID;

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(ToDoServerExtension.class)
public class GetTaskEndpointTest {
    private HttpClient httpClient;
    private AuthResponse[] authResponse;

    @BeforeEach
    public void setup() throws IOException, InterruptedException {
        authResponse = new AuthResponse[2];
        httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest1 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON_1))
                .build();

        HttpRequest httpRequest2 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON_2))
                .build();

        String responseJson = httpClient.send(httpRequest1, ofString()).body();
        authResponse[0] = JsonSerializer.fromJsonToObject(responseJson, AuthResponse.class);

        responseJson = httpClient.send(httpRequest2, ofString()).body();
        authResponse[1] = JsonSerializer.fromJsonToObject(responseJson, AuthResponse.class);
    }

    @Test
    public void shouldReturnOkForGettingTasks() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", authResponse[0].getAuth())
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.OK);
    }

    @Test
    public void shouldReturnBadRequestForEmptyHeaderGetTasks() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", "")
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    public void shouldReturnUnauthorizedForBadHeaderGetTasks() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", "xxxx:=xxxx")
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnOkForGettingTask() throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", authResponse[0].getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", authResponse[0].getAuth())
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.OK);
    }

    @Test
    public void shouldReturnBadRequestForEmptyHeaderGetTask() throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", authResponse[0].getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", "")
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    public void shouldReturnUnauthorizedForBadHeaderGetTask() throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", authResponse[0].getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", "xxxx:=xxxx")
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    public void shouldReturnForbiddenForWrongUserGetTask() throws IOException, InterruptedException {
        // given
        var httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", authResponse[0].getAuth())
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", authResponse[1].getAuth())
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.FORBIDDEN);
    }

    @Test
    public void shouldReturnNotFoundForNonExistingTask() throws IOException, InterruptedException {
        // given
        var httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + UUID.randomUUID()))
                .header("auth", authResponse[1].getAuth())
                .GET()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.NOT_FOUND);
    }
}
