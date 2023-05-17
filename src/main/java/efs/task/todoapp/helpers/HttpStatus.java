package efs.task.todoapp.helpers;

public enum HttpStatus {
    OK(200),
    CREATED(201),
    BAD_REQUEST(400),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    NOT_FOUND(404),
    CONFLICT(409);

    int statusCode;

    HttpStatus(int statusCode) {
        this.statusCode = statusCode;
    }

    public int value() {
        return statusCode;
    }
}
