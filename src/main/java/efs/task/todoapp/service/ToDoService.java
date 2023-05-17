package efs.task.todoapp.service;

import efs.task.todoapp.excpetion.BadUserException;
import efs.task.todoapp.excpetion.NoUsernameOrBadPasswordException;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.List;
import java.util.NoSuchElementException;
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

    public UUID addTask(TaskEntity taskEntity) {
        if (!userRepository.getUsers().containsKey(taskEntity.getAuth())) {
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        return taskRepository.save(taskEntity);
    }

    public List<TaskEntity> getTasks(String auth) {
        if (!userRepository.getUsers().containsKey(auth)) {
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        return taskRepository.query(task -> task.getAuth().equals(auth));
    }

    public TaskEntity getTaskById(String auth, UUID uuid) {
        if (!userRepository.getUsers().containsKey(auth)) {
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        if (!taskRepository.getTasks().containsKey(uuid)) {
            throw new NoSuchElementException("Nie ma takiego zadania");
        }

        TaskEntity taskEntity = taskRepository.query(uuid);
        if (!taskEntity.getAuth().equals(auth)) {
            throw new BadUserException("Zadanie nalezy do innego uzytkownika");
        }
        return taskEntity;
    }

    public TaskEntity updateTask(TaskEntity taskEntity, UUID uuid) {
        return taskRepository.update(uuid, taskEntity);
    }

    public boolean deleteTask(UUID id) {
        return taskRepository.delete(id);
    }
}
