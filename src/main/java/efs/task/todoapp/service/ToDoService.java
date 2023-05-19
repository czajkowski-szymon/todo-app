package efs.task.todoapp.service;

import efs.task.todoapp.excpetion.BadRequestException;
import efs.task.todoapp.excpetion.BadUserException;
import efs.task.todoapp.excpetion.NoUsernameOrBadPasswordException;
import efs.task.todoapp.excpetion.UserAlreadyAddedException;
import efs.task.todoapp.json.JsonSerializer;
import efs.task.todoapp.repository.TaskEntity;
import efs.task.todoapp.repository.TaskRepository;
import efs.task.todoapp.repository.UserEntity;
import efs.task.todoapp.repository.UserRepository;

import java.util.Base64;
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

    public String addUser(String userJson) {
        UserEntity userEntity = createUser(userJson);
        if (userRepository.getUsers().containsValue(userEntity)) {
            System.out.println("Uzytkownik od podanej nazwie juz istnieje");
            throw new UserAlreadyAddedException("Uzytkownik od podanej nazwie juz istnieje");
        }
        return userRepository.save(userEntity);
    }

    public UUID addTask(String taskJson, String auth) {
        validateAuth(auth);
        decodeAuth(auth);
        if (!userRepository.getUsers().containsKey(auth)) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        TaskEntity taskEntity = createTask(taskJson, auth);
        taskEntity.setAuth(auth);
        System.out.println("Zadanie " + taskEntity + " zostalo dodane");
        return taskRepository.save(taskEntity);
    }

    public List<TaskEntity> getTasks(String auth) {
        validateAuth(auth);
        decodeAuth(auth);
        if (!userRepository.getUsers().containsKey(auth)) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        return taskRepository.query(task -> task.getAuth().equals(auth));
    }

    public TaskEntity getTaskById(String auth, String path) {
        String uuidString = validatePath(path);
        validateAuth(auth);
        validateUUID(uuidString);
        UUID uuid = UUID.fromString(uuidString);
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

    public TaskEntity updateTask(String taskJson, String path, String auth) {
        String uuidString = validatePath(path);
        validateAuth(auth);
        decodeAuth(auth);
        if (!userRepository.getUsers().containsKey(auth)) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        validateUUID(uuidString);
        UUID uuid = UUID.fromString(uuidString);
        if (!taskRepository.getTasks().containsKey(uuid)) {
            System.out.println("Nie ma takiego zadania");
            throw new NoSuchElementException("Nie ma takiego zadania");
        }
        if (!taskRepository.query(uuid).getAuth().equals(auth)) {
            System.out.println("Zadanie o id: " + uuid + " nalezy do innego uzytkownika");
            throw new BadUserException("Zadanie nalezy do innego uzytkownika");
        }
        TaskEntity taskEntity = createTask(taskJson, auth);
        taskEntity.setAuth(auth);
        TaskEntity newTaskEntity = taskRepository.update(uuid, taskEntity);
        System.out.println("Zadanie o id: " + uuid + " zostalo zaktualizowane");
        return newTaskEntity;
    }

    public void deleteTask(String auth, String path) {
        String uuidString = validatePath(path);
        validateAuth(auth);
        decodeAuth(auth);
        if (!userRepository.getUsers().containsKey(auth)) {
            System.out.println("Brak uzytkownika lub bledne haslo");
            throw new NoUsernameOrBadPasswordException("Brak uzytkownika lub bledne haslo");
        }
        validateUUID(uuidString);
        UUID uuid = UUID.fromString(uuidString);
        if (!taskRepository.getTasks().containsKey(uuid)) {
            System.out.println("Nie ma takiego zadania");
            throw new NoSuchElementException("Nie ma takiego zadania");
        }
        if (!taskRepository.query(uuid).getAuth().equals(auth)) {
            System.out.println("Zadanie o id: " + uuid + " nalezy do innego uzytkownika");
            throw new BadUserException("Zadanie nalezy do innego uzytkownika");
        }
        System.out.println("Zadanie o id: " + uuid + " zostalo usuniete");
        taskRepository.delete(uuid);
    }

    private UserEntity createUser(String userJson) {
        UserEntity userEntity = JsonSerializer.fromJsonToObject(userJson, UserEntity.class);
        boolean isUsernameNotValid = userEntity.getUsername() == null || userEntity.getUsername().isEmpty();
        boolean isPasswordNotValid = userEntity.getPassword() == null || userEntity.getPassword().isEmpty();
        if (isPasswordNotValid || isUsernameNotValid) {
            System.out.println("Brak wymaganej tresci");
            throw new BadRequestException("Brak wymaganej tresci");
        }
        return userEntity;
    }

    private TaskEntity createTask(String taskJson, String auth) {
        if (auth == null || auth.isEmpty()) {
            System.out.println("Brak naglowka");
            throw new BadRequestException("Brak naglowka");
        }

        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(taskJson, TaskEntity.class);
        taskEntity.setAuth(auth);
        boolean isDescriptionNotValid = taskEntity.getTaskDescription() == null || taskEntity.getTaskDescription().isEmpty();
        boolean isDueDateNotValid = taskEntity.getDueDate() == null || taskEntity.getDueDate().toString().isEmpty();
        if (isDescriptionNotValid || isDueDateNotValid) {
            System.out.println("Brak wymaganej tresci");
            throw new BadRequestException("Brak wymaganej tresci");
        }
        return taskEntity;
    }

    private void validateAuth(String auth) {
        String[] authSegments = auth.split(":");
        if (auth.isEmpty() || authSegments.length < 2) {
            System.out.println("Brak naglowka");
            throw new BadRequestException("Brak naglowka");
        }
    }

    private void decodeAuth(String auth) {
        try {
            String[] segmentsAuth = auth.split(":");
            Base64.getDecoder().decode(segmentsAuth[0]);
            Base64.getDecoder().decode(segmentsAuth[1]);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Blad auth");
        }
    }

    private String validatePath(String path) {
        String[] segments = path.split("/todo/task/");
        if (segments.length < 2) {
            System.out.println("Brak parametru");
            throw new BadRequestException("Brak parametru");
        }
        return segments[1];
    }

    private void validateUUID(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            System.out.println("Brak parametru");
            throw new BadRequestException("Brak parametru");
        }
    }

}
