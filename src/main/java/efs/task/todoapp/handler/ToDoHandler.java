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
import java.util.logging.Logger;

public class ToDoHandler implements HttpHandler {
    private static final Logger LOGGER = Logger.getLogger(ToDoHandler.class.getName());
    private final ToDoService toDoService;
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
            LOGGER.info(method + " request send to " + path + " request body: " + userJson);
            addUser(userJson);
        } else if (method.equals("POST") && path.equals("/todo/task")) {
            String taskJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            LOGGER.info(method + " request send from " + auth + " to " + path + " request body: " + taskJson);
            addTask(taskJson, auth);
        } else if (method.equals("GET") && path.equals("/todo/task")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            LOGGER.info(method + " request send from " + auth + " to " + path);
            getTasks(auth);
        } else if (method.equals("GET") && path.startsWith("/todo/task/")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            LOGGER.info(method + " request send from " + auth + " to " + path);
            getTaskById(auth, path);
        } else if (method.equals("PUT") && path.startsWith("/todo/task")) {
            String taskJson = new String(httpExchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            LOGGER.info(method + " request send from " + auth + " to " + path + " request body: " + taskJson);
            updateTask(taskJson, auth, path);
        } else if (method.equals("DELETE") && path.startsWith("/todo/task")) {
            String auth = httpExchange.getRequestHeaders().getFirst("auth");
            LOGGER.info(method + " request send from " + auth + " to " + path);
            deleteTask(auth, path);
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

    private void addUser(String userJson) {
        try {
            statusCode = HttpStatus.CREATED;
            response = JsonSerializer.fromObjectToJson(new AuthResponse(toDoService.addUser(userJson)));
            LOGGER.info("User added, sending code: " + statusCode.value());
        } catch (UserAlreadyAddedException e) {
            statusCode = HttpStatus.CONFLICT;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("User already added, sending code: " + statusCode.value());
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info(e.getMessage() + ", sending code: " + statusCode.value());
        }
    }

    private void addTask(String taskJson, String auth) {
        try {
            statusCode = HttpStatus.CREATED;
            response = JsonSerializer.fromObjectToJson(new UUIDResponse(toDoService.addTask(taskJson, auth)));
            LOGGER.info("Task created, sending code: " + statusCode.value());
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Wrong username or password, sending code: " + statusCode.value());
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info(e.getMessage() + ", sending code: " + statusCode.value());
        }
    }

    private void getTasks(String auth) {
        try {
            statusCode = HttpStatus.OK;
            response = JsonSerializer.fromObjectToJson(toDoService.getTasks(auth));
            LOGGER.info("Tasks successfully returned, sending code: " + statusCode.value());
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Wrong username or password, sending code: " + statusCode.value());
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info( e.getMessage() + ", sending code: " + statusCode.value());
        }
    }

    private void getTaskById(String auth, String path) {
        try {
            statusCode = HttpStatus.OK;
            response = JsonSerializer.fromObjectToJson(toDoService.getTaskById(auth, path));
            LOGGER.info("Task successfully returned, sending code: " + statusCode.value());
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Wrong username or password, sending code: " + statusCode.value());
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Task does not belong to given user, sending code: " + statusCode.value());
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Task not found, sending code: " + statusCode.value());
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info(e.getMessage() + ", sending code: " + statusCode.value());
        }
    }

    private void updateTask(String taskJson, String auth, String path) {
        try {
            statusCode = HttpStatus.OK;
            response = JsonSerializer.fromObjectToJson(toDoService.updateTask(taskJson, path, auth));
            LOGGER.info("Task successfully updated, sending code: " + statusCode.value());
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Wrong username or password, sending code: " + statusCode.value());
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Task does not belong to given user, sending code: " + statusCode.value());
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Task not found, sending code: " + statusCode.value());
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info(e.getMessage() + ", sending code: " + statusCode.value());
        }
    }

    private void deleteTask(String auth, String path) {
        try {
            statusCode = HttpStatus.OK;
            toDoService.deleteTask(auth, path);
            response = "Task deleted";
            LOGGER.info("Task successfully deleted, sending code: " + statusCode.value());
        } catch (NoUsernameOrBadPasswordException e) {
            statusCode = HttpStatus.UNAUTHORIZED;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Wrong username or password, sending code: " + statusCode.value());
        } catch (BadUserException e) {
            statusCode = HttpStatus.FORBIDDEN;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Task does not belong to given user, sending code: " + statusCode.value());
        } catch (NoSuchElementException e) {
            statusCode = HttpStatus.NOT_FOUND;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info("Task not found, sending code: " + statusCode.value());
        } catch (BadRequestException e) {
            statusCode = HttpStatus.BAD_REQUEST;
            response = JsonSerializer.fromObjectToJson(new ErrorResponse(statusCode.value(), e.getMessage()));
            LOGGER.info(e.getMessage() + ", sending code: " + statusCode.value());
        }
    }
}
