package efs.task.todoapp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.excpetion.*;
import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.UUIDResponse;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.service.ToDoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
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
        System.out.println(method);
        System.out.println(path);
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
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            response = getTasks(auth);
        } else if (method.equals("GET") && path.startsWith("/todo/task/")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            response = getTaskById(auth, path);
        } else if (method.equals("PUT") && path.startsWith("/todo/task")) {
            String taskJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            System.out.println(taskJson);
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            response = updateTask(taskJson, auth, path);
        } else if (method.equals("DELETE") && path.startsWith("/todo/task")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            response = deleteTask(auth, path);
        } else {
            statusCode = HttpStatus.NOT_FOUND;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), "Page not found"));
        }
        System.out.println(statusCode.value());
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
            statusCode = HttpStatus.CREATED;
            return JsonSerializer.fromObjectToJson(new AuthResponse(toDoService.addUser(userEntity)));
        } catch (UserAlreadyAddedException e) {
            statusCode = HttpStatus.CONFLICT;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
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
            System.out.println("Brak wymaganej tresci");
            throw new BadRequestException("Brak wymaganej tresci");
        }
        return userEntity;
    }

    private String addTask(String taskJson, String auth) {
        try {
            validateAuth(auth);
            statusCode = HttpStatus.CREATED;
            return JsonSerializer.fromObjectToJson(new UUIDResponse(toDoService.addTask(taskJson, auth)));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        }
    }

    private String getTasks(String auth) {
        try {
            validateAuth(auth);
            statusCode = HttpStatus.OK;
            return JsonSerializer.fromObjectToJson(toDoService.getTasks(auth));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        }
    }

    private String getTaskById(String auth, String path) {
        try {
            String uuidString = validatePath(path);
            validateAuth(auth);
            validateUUID(uuidString);
            UUID uuid = UUID.fromString(uuidString);
            statusCode = HttpStatus.OK;
            return JsonSerializer.fromObjectToJson(toDoService.getTaskById(auth, uuid));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (IllegalArgumentException e ) {
            statusCode = HttpStatus.BAD_REQUEST;
            System.out.println("Nie ma takiego zadania");
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), "Nie ma takiego zadania"));
        }
    }

    private String updateTask(String taskJson, String auth, String path) {
        try {
            String uuidString = validatePath(path);
            validateAuth(auth);
            validateUUID(uuidString);
            UUID uuid = UUID.fromString(uuidString);
            statusCode = HttpStatus.OK;
            return JsonSerializer.fromObjectToJson(toDoService.updateTask(taskJson, auth, uuid));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (IllegalArgumentException e ) {
            statusCode = HttpStatus.BAD_REQUEST;
            System.out.println("Nie ma takiego zadania");
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), "Nie ma takiego zadania"));
        }
    }

    private String deleteTask(String auth, String path) {
        try {
            String uuidString = validatePath(path);
            validateAuth(auth);
            validateUUID(uuidString);
            UUID uuid = UUID.fromString(uuidString);
            statusCode = HttpStatus.OK;
            toDoService.deleteTask(auth, uuid);
            return "";
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (IllegalArgumentException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            System.out.println("Zle uuid");
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), "Zle uuid"));
        }
    }

    private void validateAuth(String auth) {
        String[] authSegments = auth.split(":");
        if (auth.isEmpty() || authSegments.length < 2) {
            System.out.println("Brak naglowka");
            throw new BadRequestException("Brak naglowka");
        }
    }

    private void validateUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            System.out.println("Brak parametru");
            throw new BadRequestException("Brak parametru");
        }
    }

    private String validatePath(String path) {
        String[] segments = path.split("/todo/task/");
        if (segments.length < 2) {
            System.out.println("Brak parametru");
            throw new BadRequestException("Brak parametru");
        }
        return segments[1];
    }
}
