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
        userEntity.setAuth(encodeAuth(userEntity.getUsername(), userEntity.getPassword()));
        if (userExists(userEntity.getAuth())) {
            throw new UserAlreadyAddedException("User with given name already exists");
        }

        return userRepository.save(userEntity);
    }

    public UUID addTask(String taskJson, String auth) {
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        if (!userExists(auth)) {
            throw new NoUsernameOrBadPasswordException("Wrong username or password");
        }

        return taskRepository.save(createTask(taskJson, auth));
    }

    public List<TaskEntity> getTasks(String auth) {
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        if (!userExists(auth)) {
            throw new NoUsernameOrBadPasswordException("Wrong username or password");
        }

        return taskRepository.query(task -> task.getAuth().equals(auth));
    }

    public TaskEntity getTaskById(String auth, String path) {
        UUID uuid;
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        if (isPathValid(path)) {
            uuid = UUID.fromString(getUUID(path));
        } else {
            throw new BadRequestException("Empty uuid");
        }
        if (!userExists(auth)) {
            throw new NoUsernameOrBadPasswordException("Wrong username or password");
        }
        if (!taskExists(uuid)) {
            throw new NoSuchElementException("Task with given uuid does not exist");
        }
        if (!taskBelongToUser(uuid, auth)) {
            throw new BadUserException("Task does not belong to given user");
        }
        return taskRepository.query(uuid);
    }

    public TaskEntity updateTask(String taskJson, String path, String auth) {
        UUID uuid;
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        if (isPathValid(path)) {
            uuid = UUID.fromString(getUUID(path));
        } else {
            throw new BadRequestException("Empty uuid");
        }
        if (!userExists(auth)) {
            throw new NoUsernameOrBadPasswordException("Wrong username or password");
        }
        if (!taskExists(uuid)) {
            throw new NoSuchElementException("Task with given uuid does not exist");
        }
        if (!taskBelongToUser(uuid, auth)) {
            throw new BadUserException("Task does not belong to given user");
        }

        return taskRepository.update(uuid, createTask(taskJson, auth));
    }

    public void deleteTask(String auth, String path) {
        UUID uuid;
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        if (isPathValid(path)) {
            uuid = UUID.fromString(getUUID(path));
        } else {
            throw new BadRequestException("Empty uuid");
        }
        if (!userExists(auth)) {
            throw new NoUsernameOrBadPasswordException("Wrong username or password");
        }
        if (!taskExists(uuid)) {
            throw new NoSuchElementException("Task with given uuid does not exist");
        }
        if (!taskBelongToUser(uuid, auth)) {
            throw new BadUserException("Task does not belong to given user");
        }

        taskRepository.delete(uuid);
    }

    private UserEntity createUser(String userJson) {
        UserEntity userEntity = JsonSerializer.fromJsonToObject(userJson, UserEntity.class);
        boolean isUsernameNotValid = userEntity.getUsername() == null || userEntity.getUsername().isEmpty();
        boolean isPasswordNotValid = userEntity.getPassword() == null || userEntity.getPassword().isEmpty();
        if (isPasswordNotValid || isUsernameNotValid) {
            throw new BadRequestException("Bad request body");
        }

        return userEntity;
    }

    private TaskEntity createTask(String taskJson, String auth) {
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(taskJson, TaskEntity.class);
        taskEntity.setAuth(auth);
        boolean isDescriptionNotValid = taskEntity.getTaskDescription() == null || taskEntity.getTaskDescription().isEmpty();
        boolean isDueDateNotValid = taskEntity.getDueDate() == null || taskEntity.getDueDate().toString().isEmpty();
        if (isDescriptionNotValid && isDueDateNotValid) {
            throw new BadRequestException("Bad request body");
        }
        
        return taskEntity;
    }

    private boolean isAuthValid(String auth) {
        try {
            String[] authSegments = auth.split(":");
            if (authSegments.length < 2) {
                return false;
            }
            Base64.getDecoder().decode(authSegments[0]);
            Base64.getDecoder().decode(authSegments[1]);
            return true;
        } catch (NullPointerException | IllegalArgumentException e) {
            return false;
        }
    }

    private boolean isPathValid(String path) {
        return path.split("/todo/task/").length == 2;
    }

    private String getUUID(String path) {
        String[] segments = path.split("/todo/task/");
        return segments[1];
    }

    private String encodeAuth(String username, String password) {
        return Base64.getEncoder().encodeToString(username.getBytes())
                + ":"
                + Base64.getEncoder().encodeToString(password.getBytes());
    }

    private boolean userExists(String auth) {
        return userRepository.getUsers().containsKey(auth);
    }

    private boolean taskExists(UUID uuid) {
        return taskRepository.getTasks().containsKey(uuid);
    }

    private boolean taskBelongToUser(UUID uuid, String auth) {
        return taskRepository.query(uuid).getAuth().equals(auth);
    }
}
