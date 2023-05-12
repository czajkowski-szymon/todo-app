package efs.task.todoapp.excpetion;

public class BadJSONException extends RuntimeException {
    public BadJSONException(String message) {
        super(message);
    }
}
