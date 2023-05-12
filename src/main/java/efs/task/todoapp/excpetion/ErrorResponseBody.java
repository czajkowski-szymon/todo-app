package efs.task.todoapp.excpetion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ErrorResponseBody {
    @JsonProperty("status")
    private int statusCode;

    @JsonProperty("message")
    private String message;

    public ErrorResponseBody(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public ErrorResponseBody() {
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
