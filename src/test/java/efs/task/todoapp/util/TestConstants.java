package efs.task.todoapp.util;

import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.TaskEntity;

public class TestConstants {
    public static final int OK = HttpStatus.OK.value();
    public static final int CREATED = HttpStatus.CREATED.value();
    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    public static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.value();
    public static final int CONFLICT = HttpStatus.CONFLICT.value();
    public static final int FORBIDDEN = HttpStatus.FORBIDDEN.value();
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
    public static final String TODO_APP_PATH = "http://localhost:8080/todo/";
    public static final String[] TASK_JSON =  {
            "{\"description\": \"buy water\", \"due\": \"2021-06-30\"}",
            "{\"description\": \"buy bread\"}"
    };
    public static final TaskEntity[] TASK_OBJECT = {
            JsonSerializer.fromJsonToObject(TASK_JSON[0], TaskEntity.class),
            JsonSerializer.fromJsonToObject(TASK_JSON[1], TaskEntity.class)
    };
    public static final String[] USER_JSON = {
            "{\"username\": \"name\", \"password\": \"passwd\"}",
            "{\"username\": \"name2\", \"password\": \"passwd\"}"
    };
    public static final String[] USER_AUTH = {
            "bmFtZQ==:cGFzc3dk",
            "bmFtZTI=:cGFzc3dk"
    };
}
