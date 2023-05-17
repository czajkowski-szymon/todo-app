package efs.task.todoapp.excpetion;

public class BadUserException extends RuntimeException {
    public BadUserException(String message) {
        super(message);
    }
}
