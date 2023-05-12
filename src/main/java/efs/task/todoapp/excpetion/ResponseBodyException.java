package efs.task.todoapp.excpetion;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ResponseBodyException {
    @JsonProperty("status")
    private int statusCode;

    @JsonProperty("message")
    private String message;

    public ResponseBodyException(int statusCode, String message) {
        this.statusCode = statusCode;
        this.message = message;
    }

    public ResponseBodyException() {
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
