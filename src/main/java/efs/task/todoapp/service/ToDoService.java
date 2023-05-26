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
        return userRepository.save(userEntity);
    }

    public UUID addTask(String taskJson, String auth) {
        TaskEntity taskEntity = createTask(taskJson, auth);
        if (!userExists(auth)) {
            throw new NoUsernameOrBadPasswordException("Wrong username or password");
        }
        return taskRepository.save(taskEntity);
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
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        UUID uuid = getUUID(path);
        validateUserAndTask(uuid, auth);

        return taskRepository.query(uuid);
    }

    public TaskEntity updateTask(String taskJson, String path, String auth) {
        TaskEntity taskEntity = createTask(taskJson, auth);
        UUID uuid = getUUID(path);
        validateUserAndTask(uuid, auth);

        return taskRepository.update(uuid, taskEntity);
    }

    public void deleteTask(String auth, String path) {
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        UUID uuid = getUUID(path);
        validateUserAndTask(uuid, auth);

        taskRepository.delete(uuid);
    }

    private UserEntity createUser(String userJson) {
        UserEntity userEntity = JsonSerializer.fromJsonToObject(userJson, UserEntity.class);
        if ((userEntity.getUsername() == null || userEntity.getUsername().isEmpty()) ||
                (userEntity.getPassword() == null || userEntity.getPassword().isEmpty())) {
            throw new BadRequestException("Bad request body");
        }
        userEntity.setAuth(encodeAuth(userEntity.getUsername(), userEntity.getPassword()));
        if (userExists(userEntity.getAuth())) {
            throw new UserAlreadyAddedException("User with given name already exists");
        }
        return userEntity;
    }

    private TaskEntity createTask(String taskJson, String auth) {
        if (!isAuthValid(auth)) {
            throw new BadRequestException("Wrong header");
        }
        TaskEntity taskEntity = JsonSerializer.fromJsonToObject(taskJson, TaskEntity.class);
        taskEntity.setAuth(auth);
        if ((taskEntity.getTaskDescription() == null || taskEntity.getTaskDescription().isEmpty()) &&
                (taskEntity.getDueDate() == null || taskEntity.getDueDate().toString().isEmpty())) {
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

    private UUID getUUID(String path) {
        try {
            String[] segments = path.split("/todo/task/");
            if (segments.length != 2) {
                throw new BadRequestException("Empty uuid");
            }
            return UUID.fromString(segments[1]);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException("Wrong uuid");
        }
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

    private void validateUserAndTask(UUID uuid, String auth) {
        if (!userExists(auth)) {
            throw new NoUsernameOrBadPasswordException("Wrong username or password");
        }
        if (!taskExists(uuid)) {
            throw new NoSuchElementException("Task with given uuid does not exist");
        }
        if (!taskBelongToUser(uuid, auth)) {
            throw new BadUserException("Task does not belong to given user");
        }
    }
}
