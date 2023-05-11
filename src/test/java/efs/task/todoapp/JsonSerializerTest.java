package efs.task.todoapp;

import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class JsonSerializerTest {
    private static final String REFERENCE_USER_JSON = "{\"username\":\"user\",\"password\":\"password\"}";
    private static final String REFERENCE_TASK_JSON = "{\"description\":\"Wyjdz na spacer z psem\",\"due\":\"2022-12-03\"}";

    @Test
    public void shouldReturnCorrectJsonForUser() {
        // given
        UserEntity referenceUser = new UserEntity("user", "password");

        // when
        String json = JsonSerializer.fromObjectToJson(referenceUser);

        // then
        assertEquals(REFERENCE_USER_JSON, json);
    }

    @Test
    public void shouldReturnCorrectJsonForTask() {
        // given
        TaskEntity referenceTask = new TaskEntity("Wyjdz na spacer z psem", "2022-12-03");

        // when
        String json = JsonSerializer.fromObjectToJson(referenceTask);

        // then
        assertEquals(REFERENCE_TASK_JSON, json);
    }
}
