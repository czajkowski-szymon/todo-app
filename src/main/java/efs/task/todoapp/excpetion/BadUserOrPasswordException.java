package efs.task.todoapp.excpetion;

public class BadUserOrPasswordException extends RuntimeException {
    public BadUserOrPasswordException(String message) {
        super(message);
    }
}
