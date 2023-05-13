package efs.task.todoapp.util;

import com.sun.net.httpserver.HttpServer;
import efs.task.todoapp.ToDoApplication;
import efs.task.todoapp.handler.ToDoHandler;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserRepository;
import efs.task.todoapp.service.ToDoService;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.io.IOException;

public class ToDoServerExtension implements Extension, BeforeEachCallback, AfterEachCallback {
    private HttpServer server;

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws IOException {
        var todoApplication = new ToDoApplication();
        server = todoApplication.createServer();
        ToDoHandler toDoHandler = new ToDoHandler(new ToDoService(new UserRepository(), new TaskRepository()));
        server.createContext("/todo/user", toDoHandler);
        server.createContext("/todo/task", toDoHandler);
        server.setExecutor(null);
        server.start();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) {
        server.stop(0);
    }
}
