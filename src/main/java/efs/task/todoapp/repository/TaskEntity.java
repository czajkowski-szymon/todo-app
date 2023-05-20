package efs.task.todoapp.repository;

import com.fasterxml.jackson.annotation.*;

import java.util.Date;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TaskEntity {
    @JsonIgnore
    private String auth;

    @JsonProperty("id")
    private UUID uuid;

    @JsonProperty("description")
    private String taskDescription;

    @JsonProperty("due")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private Date dueDate;

    public TaskEntity(String taskDescription, Date dueDate) {
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
    }

    public TaskEntity() {
    }

    public String getAuth() {
        return auth;
    }

    public void setAuth(String auth) {
        this.auth = auth;
    }

    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setDueDate(Date dueDate) {
        this.dueDate = dueDate;
    }

    public void update(String taskDescription, Date dueDate) {
        setTaskDescription(taskDescription);
        setDueDate(dueDate);
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "taskDescription='" + taskDescription + '\'' +
                ", dueDate='" + dueDate + '\'' +
                '}';
    }
}
