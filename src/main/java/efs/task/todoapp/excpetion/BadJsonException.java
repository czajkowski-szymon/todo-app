package efs.task.todoapp.excpetion;

public class BadJsonException extends RuntimeException {
    public BadJsonException(String message) {
        super(message);
    }
}
