package efs.task.todoapp;

import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.handler.ToDoHandler;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserRepository;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.web.WebServerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.logging.Logger;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class ToDoApplication {
    private static final Logger LOGGER = Logger.getLogger(ToDoApplication.class.getName());

    public static void main(String[] args) throws IOException, InterruptedException {
        var application = new ToDoApplication();
        HttpServer server = null;
        try {
            server = application.createServer();
        } catch (IOException e) {
            LOGGER.info("ToDoApplication's server failed to start");
        }
        ToDoService toDoService = new ToDoService(new UserRepository(), new TaskRepository());
        ToDoHandler toDoHandler = new ToDoHandler(toDoService);
        server.createContext("/todo/user", toDoHandler);
        server.createContext("/todo/task", toDoHandler);
        server.setExecutor(null);
        server.start();

        LOGGER.info("ToDoApplication's server started ...");
        System.out.println("Server started");
    }

    public HttpServer createServer() throws IOException {
        return WebServerFactory.createServer();
    }
}
