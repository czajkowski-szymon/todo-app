package efs.task.todoapp.web;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;

public class WebServerFactory {
    private static final int HTTP_PORT = 8080;
    public static HttpServer createServer() throws IOException {
        return HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
    }
}
