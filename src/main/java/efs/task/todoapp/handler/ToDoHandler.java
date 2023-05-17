package efs.task.todoapp.handler;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.excpetion.*;
import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UUIDResponse;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.ToDoService;

import javax.swing.plaf.IconUIResource;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
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

        if (method.equals("OPTIONS") && path.startsWith("/todo/")) {
            statusCode = HttpStatus.OK;
            response = "";
        } else if (method.equals("POST") && path.equals("/todo/user")) {
            String userJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            response = addUser(userJson);
        } else if (method.equals("POST") && path.equals("/todo/task")) {
            String taskJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            response = addTask(taskJson, auth);
        } else if (method.equals("GET") && path.equals("/todo/task")) {
            Headers headers = httpExchange.getRequestHeaders();
            String auth = headers.getFirst("auth");
            response = getTasks(auth);
        } else if (method.equals("GET") && path.startsWith("/todo/task/")) {
            UUID uuid = UUID.fromString(path.split("/todo/task/")[1]);
            response = getTaskById(uuid);
            statusCode = HttpStatus.OK;
        } else if (method.equals("PUT") && path.startsWith("/todo/task/")) {
            UUID uuid = UUID.fromString(path.split("/todo/task/")[1]);
            String taskJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            response = updateTask(taskJson, auth, uuid);
            statusCode = HttpStatus.OK;
        } else if (method.equals("DELETE") && path.startsWith("/todo/task/")) {
            UUID uuid = UUID.fromString(path.split("/todo/task/")[1]);
            response = deleteTask(uuid);
        } else {
            response = "Page not found";
            statusCode = HttpStatus.NOT_FOUND;
        }

        httpExchange.getResponseHeaders().set("Content-Type", "application/json");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Headers", "auth, Content-Type, Accept, X-Requested-With");
        httpExchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT, OPTIONS, HEAD");
        httpExchange.sendResponseHeaders(statusCode.value(), response.getBytes().length);
        httpExchange.getResponseBody().write(response.getBytes());
        httpExchange.close();
    }

    private String addUser(String userJson) {
        try {
            UserEntity userEntity = createUser(userJson);
            try {
                statusCode = HttpStatus.CREATED;
                return JsonSerializer.fromObjectToJson(new AuthResponse(toDoService.addUser(userEntity)));
            } catch (UserAlreadyAddedException e) {
                statusCode = HttpStatus.CONFLICT;
                return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            }
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        }
    }

    private UserEntity createUser(String userJson) {
        UserEntity userEntity = JsonSerializer.fromJsonToObject(userJson, UserEntity.class);
        boolean isUsernameNotValid = userEntity.getUsername() == null || userEntity.getUsername().isEmpty();
        boolean isPasswordNotValid = userEntity.getPassword() == null || userEntity.getPassword().isEmpty();
        if (isPasswordNotValid || isUsernameNotValid) {
            statusCode = HttpStatus.BAD_REQUEST;
            throw new BadRequestException("Brak wymaganej tresci");
        }
        return userEntity;
    }

    private String addTask(String taskJson, String auth) {
        try {
            TaskEntity taskEntity = createTask(taskJson, auth);
            try {
                statusCode = HttpStatus.CREATED;
                return JsonSerializer.fromObjectToJson(new UUIDResponse(toDoService.addTask(taskEntity)));
            } catch (BadUserOrPasswordException e) {
                statusCode = HttpStatus.UNAUTHORIZED;
                return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            }
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        }
    }

    private TaskEntity createTask(String taskJson, String auth) {
        if (auth == null || auth.isEmpty()) {
            System.out.println("Brak naglowka");
            throw new BadRequestException("Brak naglowka");
        }

        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(taskJson, TaskEntity.class);
        taskEntity.setAuth(auth);
        boolean isDescriptionNotValid = taskEntity.getTaskDescription() == null || taskEntity.getTaskDescription().isEmpty();
        boolean isDueDateNotValid = taskEntity.getDueDate() == null || taskEntity.getDueDate().isEmpty();
        if (isDescriptionNotValid || isDueDateNotValid) {
            System.out.println("Brak wymaganej tresci");
            throw new BadRequestException("Brak wymaganej tresci");
        }
        return taskEntity;
    }

    private String getTasks(String auth) {
        return JsonSerializer.fromObjectToJson(toDoService.getTasks(auth));
    }

    private String getTaskById(UUID uuid) {
        return JsonSerializer.fromObjectToJson(toDoService.getTaskById(uuid));
    }

    private String updateTask(String taskJson, String auth, UUID uuid) {
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(taskJson, TaskEntity.class);
        taskEntity.setAuth(auth);
        return JsonSerializer.fromObjectToJson(toDoService.updateTask(taskEntity, uuid));
    }

    private String deleteTask(UUID uuid) {
        if (toDoService.deleteTask(uuid)){
            statusCode = HttpStatus.OK;
            return "Usunieto";
        } else {
            statusCode = HttpStatus.BAD_REQUEST;
            return "Nie usunieto";
        }
    }
}
