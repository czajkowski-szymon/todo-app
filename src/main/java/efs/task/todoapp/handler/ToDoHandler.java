package efs.task.todoapp.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.excpetion.BadJsonException;
import efs.task.todoapp.excpetion.BadUserOrPasswordException;
import efs.task.todoapp.excpetion.ErrorResponseBody;
import efs.task.todoapp.excpetion.UserAlreadyAddedException;
import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.ToDoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;

public class ToDoHandler implements HttpHandler {
    private ToDoService toDoService;
    private String response;
    private HttpStatus statusCode;

    public ToDoHandler(ToDoService toDoService) {
        this.toDoService = toDoService;
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type, Accept, X-Requested-With");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");

        if (method.equals("OPTIONS") && path.startsWith("/todo/")) {
            statusCode = HttpStatus.OK;
            response = "";
        } else if (method.equals("POST") && path.equals("/todo/user")) {
            String userJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            response = addUser(userJson);
        } else if (method.equals("POST") && path.equals("/todo/task")) {
            String taskJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            Headers headers = httpExchange.getRequestHeaders();
            String auth = headers.getFirst("auth");
            System.out.println(auth);
            response = addTask(taskJson, auth);
        } else if (method.equals("GET") && path.equals("/todo/task")) {
            response = "haha";
            statusCode = HttpStatus.OK;
        } else if (method.equals("GET") && path.startsWith("/todo/task/")) {
            UUID id = UUID.fromString(path.split("/todo/task/")[1]);
            getTaskById(id);
            statusCode = HttpStatus.OK;
        } else if (method.equals("PUT") && path.startsWith("/todo/task/")) {
            UUID id = UUID.fromString(path.split("/todo/task/")[1]);
            updateTask(id);
            statusCode = HttpStatus.OK;
        } else if (method.equals("DELETE") && path.startsWith("/todo/task/")) {
            UUID id = UUID.fromString(path.split("/todo/task/")[1]);
            deleteTask(id);
            statusCode = HttpStatus.OK;
        } else {
            response = "Page not found";
            statusCode = HttpStatus.NOT_FOUND;
        }

        httpExchange.sendResponseHeaders(statusCode.getStatusCode(), response.getBytes().length);
        httpExchange.getResponseBody().write(response.getBytes());
        httpExchange.close();
    }

    private String addUser(String userJson) {
        try {
            UserEntity userEntity = JsonSerializer.fromJsonToObject(userJson, UserEntity.class);
            try {
                statusCode = HttpStatus.CREATED;
                return "{\"auth\": \"" + toDoService.addUser(userEntity) + "\"}";
            } catch (UserAlreadyAddedException e) {
                statusCode = HttpStatus.CONFLICT;
                return JsonSerializer.fromObjectToJson(
                        new ErrorResponseBody(HttpStatus.CONFLICT.getStatusCode(), e.getMessage()));
            }
        } catch (BadJsonException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(
                    new ErrorResponseBody(HttpStatus.BAD_REQUEST.getStatusCode(), e.getMessage()));
        }
    }

    private String addTask(String taskJson, String auth) {
        if (auth == null) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(
                    new ErrorResponseBody(HttpStatus.BAD_REQUEST.getStatusCode(), "Brak naglowka"));
        }
        try {
            TaskEntity taskEntity = JsonSerializer.fromJsonToObject(taskJson, TaskEntity.class);
            taskEntity.setAuth(auth);
            try {
                statusCode = HttpStatus.CREATED;
                return "{\"id\": \"" + toDoService.addTask(taskEntity) + "\"}";
            } catch (BadUserOrPasswordException e) {
                statusCode = HttpStatus.UNAUTHORIZED;
                return JsonSerializer.fromObjectToJson(
                        new ErrorResponseBody(HttpStatus.UNAUTHORIZED.getStatusCode(), e.getMessage()));
            }
        } catch (BadJsonException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(
                    new ErrorResponseBody(HttpStatus.BAD_REQUEST.getStatusCode(), e.getMessage()));
        }
    }

    private List<String> getTasks() {
        return toDoService.getTasks();
    }

    private TaskEntity getTaskById(UUID id) {
        return getTaskById(id);
    }

    private void updateTask(UUID id) {
        toDoService.updateTask(id);
    }

    private void deleteTask(UUID id) {
        toDoService.deleteTask(id);
    }
}
