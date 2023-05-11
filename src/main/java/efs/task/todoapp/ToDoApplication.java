package efs.task.todoapp;

import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.handler.ToDoHandler;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserRepository;
import efs.task.todoapp.service.ToDoService;
import efs.task.todoapp.web.WebServerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.Logger;

public class ToDoApplication {
    private static final Logger LOGGER = Logger.getLogger(ToDoApplication.class.getName());

    public static void main(String[] args) throws IOException {
        var application = new ToDoApplication();
        var server = application.createServer();
        ToDoHandler toDoHandler = new ToDoHandler(new ToDoService(new UserRepository(), new TaskRepository()));
        server.createContext("/todo/user", toDoHandler);
        server.createContext("/todo/task", toDoHandler);
        server.setExecutor(null);
        server.start();

        LOGGER.info("ToDoApplication's server started ...");
    }

    public HttpServer createServer() throws IOException {
        return WebServerFactory.createServer();
    }
}
