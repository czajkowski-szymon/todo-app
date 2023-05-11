package efs.task.todoapp.repository;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

import java.util.UUID;


public class TaskEntity {
    @JsonProperty("description")
    private String taskDescription;
    @JsonProperty("due")
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private String dueDate;

    public TaskEntity(String taskDescription, String dueDate) {
        this.taskDescription = taskDescription;
        this.dueDate = dueDate;
    }

    public TaskEntity() {
    }

    public String getTaskDescription() {
        return taskDescription;
    }

    public void setTaskDescription(String taskDescription) {
        this.taskDescription = taskDescription;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    @Override
    public String toString() {
        return "TaskEntity{" +
                "taskDescription='" + taskDescription + '\'' +
                ", dueDate='" + dueDate + '\'' +
                '}';
    }
}
