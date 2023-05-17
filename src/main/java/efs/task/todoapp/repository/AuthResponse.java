package efs.task.todoapp.repository;

public class AuthResponse {
    private String auth;

    public AuthResponse() {
    }

    public AuthResponse(String auth) {
        this.auth = auth;
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }
}
