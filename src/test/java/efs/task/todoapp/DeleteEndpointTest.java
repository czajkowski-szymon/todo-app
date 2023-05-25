package efs.task.todoapp;

import efs.task.todoapp.json.JsonSerializer;
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
public class DeleteEndpointTest {
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

    // TODO: Test sprawdzajacy czy zadanie jest usuwane

    @ParameterizedTest(name = "header = {0}")
    @CsvFileSource(resources = {"/badheaders.csv"})
    @Timeout(1)
    public void shouldReturnBadRequestForBadHeaderDelete(String header) throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", header)
                .DELETE()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    @Timeout(1)
    public void shouldReturnBadRequestForBadPathDelete() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        httpClient.send(httpRequestPOST, ofString());

        HttpRequest httpRequestDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .DELETE()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequestDELETE, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    @Timeout(1)
    public void shouldReturnUnauthorizedForWrongUsernameOrPasswordDelete() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestPOST = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        HttpRequest httpRequestDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", TestConstants.USER_AUTH[1])
                .DELETE()
                .build();

        // when
        HttpResponse<String> httpResponse = httpClient.send(httpRequestDELETE, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.UNAUTHORIZED);
    }

    @Test
    @Timeout(1)
    public void shouldReturnForbiddenForWrongUserDelete() throws IOException, InterruptedException {
        // given
        addSecondUser();

        HttpRequest httpRequestPOST1 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[0])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        String httpResponsePOST = httpClient.send(httpRequestPOST1, ofString()).body();
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(httpResponsePOST, TaskEntity.class);
        String uuid = taskEntity.getUuid().toString();

        HttpRequest httpRequestPOST2 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task"))
                .header("auth", TestConstants.USER_AUTH[1])
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.TASK_JSON[0]))
                .build();

        httpClient.send(httpRequestPOST2, ofString());

        HttpRequest httpRequestDELETE = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + uuid))
                .header("auth", TestConstants.USER_AUTH[1])
                .DELETE()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestDELETE, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.FORBIDDEN);
    }

    @Test
    @Timeout(1)
    public void shouldReturnNotFoundForNonExistingTaskPut() throws IOException, InterruptedException {
        // given
        HttpRequest httpRequestGET = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "task/" + UUID.randomUUID()))
                .header("auth", TestConstants.USER_AUTH[0])
                .DELETE()
                .build();

        // when
        HttpResponse<String> httpResponseGET = httpClient.send(httpRequestGET, ofString());

        // then
        assertThat(httpResponseGET.statusCode()).isEqualTo(TestConstants.NOT_FOUND);
    }
}
