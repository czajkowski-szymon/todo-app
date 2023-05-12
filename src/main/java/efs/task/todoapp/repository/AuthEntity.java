package efs.task.todoapp.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Base64;

public class AuthEntity {
    @JsonProperty("id")
    private String id;

    public AuthEntity(String username, String password) {
        this.id = Base64.getEncoder().encodeToString(username.getBytes()) + ":" +
                Base64.getEncoder().encodeToString(password.getBytes());
    }

    public AuthEntity() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
