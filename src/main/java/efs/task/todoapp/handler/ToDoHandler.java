package efs.task.todoapp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.helpers.Responses;
import efs.task.todoapp.repository.TaskEntity;
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

        if (method.equals("POST") && path.equals("/todo/user")) {
            String json = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            response = addUser(json);
        } else if (method.equals("POST") && path.equals("/todo/task")) {
            addTask();
            statusCode = HttpStatus.OK;
        } else if (method.equals("GET") && path.equals("/todo/task")) {
            response = getTasks().toString();
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
        if (userJson.length() == 0) {
            statusCode = HttpStatus.BAD_REQUEST;
            return Responses.BAD_REQUEST_BODY;
        }

        String result = toDoService.addUser(userJson);
        if (result.equals(Responses.USER_EXISTS)) {
            statusCode = HttpStatus.CONFLICT;
            return Responses.USER_EXISTS;
        } else {
            statusCode = HttpStatus.CREATED;
            return Responses.USER_ADDED;
        }
    }

    private void addTask() {
        toDoService.addTask();
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
