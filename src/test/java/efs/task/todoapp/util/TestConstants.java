package efs.task.todoapp.util;

import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.json.JsonSerializer;

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
            "{\"description\": \"kup mleko\", \"due\": \"2021-06-30\"}",
            "{\"description\": \"kup wode\"}"
    };
    public static final String[] USER_JSON = {
            "{\"username\": \"name\", \"password\": \"xxx\"}",
            "{\"username\": \"name2\", \"password\": \"xxx\"}"
    };
    public static final String[] USER_AUTH = {
            "bmFtZQ==:eHh4",
            "bmFtZTI=:eHh4"
    };
}
