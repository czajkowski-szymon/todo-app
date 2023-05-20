package efs.task.todoapp.web;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static java.net.http.HttpResponse.BodyHandlers.ofString;

public class WebServerFactory {
    private static final int HTTP_PORT = 8080;
    public static HttpServer createServer() throws IOException {
        return HttpServer.create(new InetSocketAddress(HTTP_PORT), 0);
    }

    public static void sendTestRequest() throws IOException, InterruptedException {
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create("http://localhost:8080/todo/user"))
                .POST(HttpRequest.BodyPublishers.ofString(""))
                .build();

        httpClient.send(httpRequest, ofString());
    }
}
