package efs.task.todoapp.excpetion;

public class NoUsernameOrBadPasswordException extends RuntimeException {
    public NoUsernameOrBadPasswordException(String message) {
        super(message);
    }
}
