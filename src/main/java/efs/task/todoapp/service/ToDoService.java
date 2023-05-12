package efs.task.todoapp.service;

import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.List;
import java.util.UUID;

public class ToDoService {
    private final UserRepository userRepository;
    private final TaskRepository taskRepository;

    public ToDoService(UserRepository userRepository, TaskRepository taskRepository) {
        this.userRepository = userRepository;
        this.taskRepository = taskRepository;
    }

    public String addUser(UserEntity userEntity) {
        return userRepository.save(userEntity);
    }

    public void addTask() {
        // adding task...
    }

    public List<String> getTasks() {
        // retrieving list of tasks
        return null;
    }

    public TaskEntity getTaskById(UUID id) {
        // retrieving task with given ID
        return null;
    }

    public void updateTask(UUID id) {
        // updating task with given ID
    }

    public void deleteTask(UUID id) {
        // deleting task with given ID
    }
}
