package efs.task.todoapp.service;

import efs.task.todoapp.excpetion.BadUserOrPasswordException;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.List;
import java.util.UUID;
import java.util.function.Predicate;

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

    public String addTask(TaskEntity taskEntity) {
        if (!userRepository.getUsers().containsKey(taskEntity.getAuth())) {
            throw new BadUserOrPasswordException("Bledna nazwa uzytkownika lub haslo");
        }
        return taskRepository.save(taskEntity).toString();
    }

    public List<TaskEntity> getTasks(String auth) {
        Predicate<TaskEntity> condition = task -> task.getAuth().equals(auth);
        return taskRepository.query(condition);
    }

    public TaskEntity getTaskById(UUID uuid) {
        return taskRepository.query(uuid);
    }

    public TaskEntity updateTask(TaskEntity taskEntity, UUID uuid) {
        return taskRepository.update(uuid, taskEntity);
    }

    public boolean deleteTask(UUID id) {
        return taskRepository.delete(id);
    }
}
