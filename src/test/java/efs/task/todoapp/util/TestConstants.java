package efs.task.todoapp.util;

import efs.task.todoapp.helpers.HttpStatus;

public class TestConstants {
    public static final int OK = HttpStatus.OK.value();
    public static final int CREATED = HttpStatus.CREATED.value();
    public static final int BAD_REQUEST = HttpStatus.BAD_REQUEST.value();
    public static final int UNAUTHORIZED = HttpStatus.UNAUTHORIZED.value();
    public static final int CONFLICT = HttpStatus.CONFLICT.value();
    public static final int FORBIDDEN = HttpStatus.FORBIDDEN.value();
    public static final int NOT_FOUND = HttpStatus.NOT_FOUND.value();
    public static final String TODO_APP_PATH = "http://localhost:8080/todo/";
    public static final String TASK_JSON = "{\"description\": \"Kup mleko\", \"due\": \"2021-06-30\"}";
    public static final String USER_JSON_1 = "{\"username\": \"janKowalski\", \"password\": \"am!sK#123\"}";
    public static final String USER_JSON_2 = "{\"username\": \"piotrNowak\", \"password\": \"cb?kS$321\"}";
}
