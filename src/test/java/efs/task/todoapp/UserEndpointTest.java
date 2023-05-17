package efs.task.todoapp;

import efs.task.todoapp.helpers.HttpStatus;
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

import static java.net.http.HttpResponse.BodyHandlers.ofString;
import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(ToDoServerExtension.class)
public class UserEndpointTest {
    private HttpClient httpClient;

    @BeforeEach
    void setup() {
        httpClient = HttpClient.newHttpClient();
    }

    @Test
    public void shouldReturnCreatedForAddingUser() throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON_1))
                .build();

        //when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.CREATED);
    }

    @ParameterizedTest(name = "input {0}")
    @CsvFileSource(resources = {"/badjsonuser.csv"})
    public void shouldReturnBadRequestForBadUserBodyCsv(String input) throws IOException, InterruptedException {
        // given
        var httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(input))
                .build();

        //when
        HttpResponse<String> httpResponse = httpClient.send(httpRequest, ofString());

        // then
        assertThat(httpResponse.statusCode()).isEqualTo(TestConstants.BAD_REQUEST);
    }

    @Test
    public void shouldReturnConflictForAlreadyAddedUser() throws IOException, InterruptedException {
        // given
        var httpRequest1 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON_1))
                .build();

        var httpRequest2 = HttpRequest.newBuilder()
                .uri(URI.create(TestConstants.TODO_APP_PATH + "user"))
                .POST(HttpRequest.BodyPublishers.ofString(TestConstants.USER_JSON_1 + "user"))
                .build();

        //when
        httpClient.send(httpRequest1, ofString());

        HttpResponse<String> httpResponse2 = httpClient.send(httpRequest2, ofString());

        // then
        assertThat(httpResponse2.statusCode()).isEqualTo(TestConstants.CONFLICT);
    }
}
