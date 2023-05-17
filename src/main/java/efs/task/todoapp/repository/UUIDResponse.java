package efs.task.todoapp.repository;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.UUID;

public class UUIDResponse {
    @JsonProperty("id")
    private UUID uuid;

    public UUIDResponse() {
    }

    public UUIDResponse(UUID uuid) {
        this.uuid = uuid;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }
}
