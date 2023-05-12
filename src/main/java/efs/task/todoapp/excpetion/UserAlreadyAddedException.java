package efs.task.todoapp.excpetion;

public class UserAlreadyAddedException extends RuntimeException {
    public UserAlreadyAddedException(String message) {
        super(message);
    }
}
