package efs.task.todoapp.handler;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import efs.task.todoapp.excpetion.*;
import efs.task.todoapp.helpers.HttpStatus;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.AuthResponse;
import efs.task.todoapp.repository.UUIDResponse;
import efs.task.todoapp.service.ToDoService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;

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
            System.out.println(auth);
            response = addTask(taskJson, auth);
        } else if (method.equals("GET") && path.equals("/todo/task")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            System.out.println(auth);
            response = getTasks(auth);
        } else if (method.equals("GET") && path.startsWith("/todo/task/")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            System.out.println(auth);
            response = getTaskById(auth, path);
        } else if (method.equals("PUT") && path.startsWith("/todo/task")) {
            String taskJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            System.out.println(auth);
            response = updateTask(taskJson, auth, path);
        } else if (method.equals("DELETE") && path.startsWith("/todo/task")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            System.out.println(auth);
            response = deleteTask(auth, path);
        } else {
            statusCode = HttpStatus.NOT_FOUND;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), "Page not found"));
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
            statusCode = HttpStatus.CREATED;
            return JsonSerializer.fromObjectToJson(new AuthResponse(toDoService.addUser(userJson)));
        } catch (UserAlreadyAddedException e) {
            statusCode = HttpStatus.CONFLICT;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
        }
    }

    private String addTask(String taskJson, String auth) {
        String message;
        try {
            statusCode = HttpStatus.CREATED;
            return JsonSerializer.fromObjectToJson(new UUIDResponse(toDoService.addTask(taskJson, auth)));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            message = e.getMessage();
        }
        return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), message));
    }

    private String getTasks(String auth) {
        String message;
        try {
            statusCode = HttpStatus.OK;
            return JsonSerializer.fromObjectToJson(toDoService.getTasks(auth));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            message = e.getMessage();
        }
        return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), message));
    }

    private String getTaskById(String auth, String path) {
        String message;
        try {
            statusCode = HttpStatus.OK;
            return JsonSerializer.fromObjectToJson(toDoService.getTaskById(auth, path));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            message = e.getMessage();
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            message = e.getMessage();
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            message = e.getMessage();
        } catch (IllegalArgumentException e ) {
            statusCode = HttpStatus.BAD_REQUEST;
            System.out.println("Bledne uuid");
            message = "Bledne uuid";
        }
        return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), message));
    }

    private String updateTask(String taskJson, String auth, String path) {
        String message;
        try {
            statusCode = HttpStatus.OK;
            return JsonSerializer.fromObjectToJson(toDoService.updateTask(taskJson, path, auth));
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            message = e.getMessage();
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            message = e.getMessage();
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            message = e.getMessage();
        } catch (IllegalArgumentException e ) {
            statusCode = HttpStatus.BAD_REQUEST;
            System.out.println("Nie ma takiego zadania");
            message = "Nie ma takiego zadania";
        }
        return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), message));
    }

    private String deleteTask(String auth, String path) {
        String message;
        try {
            statusCode = HttpStatus.OK;
            toDoService.deleteTask(auth, path);
            return "";
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            message = e.getMessage();
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            message = e.getMessage();
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            message = e.getMessage();
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            message = e.getMessage();
        } catch (IllegalArgumentException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            System.out.println("Zle uuid");
            message = e.getMessage();
        }
        return JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), message));
    }
}
