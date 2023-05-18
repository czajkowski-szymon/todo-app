package efs.task.todoapp.service;

import efs.task.todoapp.excpetion.BadUserException;
import efs.task.todoapp.excpetion.NoUsernameOrBadPasswordException;
import efs.task.todoapp.excpetion.UserAlreadyAddedException;
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
        if (userRepository.getUsers().containsValue(userEntity)) {
            System.out.println("Uzytkownik od podanej nazwie juz istnieje");
            throw new UserAlreadyAddedException("Uzytkownik od podanej nazwie juz istnieje");
        }
        return userRepository.save(userEntity);
    }

    public UUID addTask(TaskEntity taskEntity) {
        if (!userRepository.getUsers().containsKey(taskEntity.getAuth())) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        System.out.println("Zadanie " + taskEntity + " zostalo dodane");
        return taskRepository.save(taskEntity);
    }

    public List<TaskEntity> getTasks(String auth) {
        if (!userRepository.getUsers().containsKey(auth)) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        return taskRepository.query(task -> task.getAuth().equals(auth));
    }

    public TaskEntity getTaskById(String auth, UUID uuid) {
        if (!userRepository.getUsers().containsKey(auth)) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        if (!taskRepository.getTasks().containsKey(uuid)) {
            System.out.println("Nie ma takiego zadania");
            throw new NoSuchElementException("Nie ma takiego zadania");
        }

        TaskEntity taskEntity = taskRepository.query(uuid);
        if (!taskEntity.getAuth().equals(auth)) {
            System.out.println("Zadanie o id: " + uuid + " nalezy do innego uzytkownika");
            throw new BadUserException("Zadanie nalezy do innego uzytkownika");
        }
        return taskEntity;
    }

    public TaskEntity updateTask(TaskEntity taskEntity, UUID uuid) {
        if (!userRepository.getUsers().containsKey(taskEntity.getAuth())) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        if (!taskRepository.getTasks().containsKey(uuid)) {
            System.out.println("Nie ma takiego zadania");
            throw new NoSuchElementException("Nie ma takiego zadania");
        }

        TaskEntity newTaskEntity = taskRepository.update(uuid, taskEntity);
        if (!newTaskEntity.getAuth().equals(taskEntity.getAuth())) {
            System.out.println("Zadanie o id: " + uuid + " nalezy do innego uzytkownika");
            throw new BadUserException("Zadanie nalezy do innego uzytkownika");
        }
        System.out.println("Zadanie o id: " + uuid + " zostalo zaktualizowane");
        return newTaskEntity;
    }

    public void deleteTask(String auth, UUID uuid) {
        if (!userRepository.getUsers().containsKey(auth)) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        if (!taskRepository.getTasks().get(uuid).getAuth().equals(auth)) {
            System.out.println("Zadanie o id: " + uuid + " nalezy do innego uzytkownika");
            throw new BadUserException("Zadanie nalezy do innego uzytkownika");
        }
        if (!taskRepository.delete(uuid)) {
            System.out.println("Nie ma takiego zadania");
            throw new NoSuchElementException("Nie ma takiego zadania");
        }
        System.out.println("Zadanie o id: " + uuid + " zostalo usuniete");
        taskRepository.delete(uuid);
    }
}
