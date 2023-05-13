package efs.task.todoapp.repository;

import efs.task.todoapp.excpetion.UserAlreadyAddedException;
import efs.task.todoapp.json.JsonSerializer;

import java.util.*;
import java.util.function.Predicate;

public class UserRepository implements Repository<String, UserEntity> {
    private Map<String, UserEntity> users;

    public UserRepository() {
        users = new HashMap<>();
    }

    public Map<String, UserEntity> getUsers() {
        return users;
    }

    @Override
    public String save(UserEntity userEntity) {
        String auth = encode(userEntity.getUsername(), userEntity.getPassword());
        if (users.containsValue(userEntity)) {
            throw new UserAlreadyAddedException("Uzytkownik od podanej nazwie juz istnieje");
        }
        users.put(auth, userEntity);
        return auth;
    }

    @Override
    public UserEntity query(String s) {
        return null;
    }

    @Override
    public List<UserEntity> query(Predicate<UserEntity> condition) {
        return null;
    }

    @Override
    public UserEntity update(String s, UserEntity userEntity) {
        return null;
    }

    @Override
    public boolean delete(String s) {
        return false;
    }

    private String encode(String username, String password) {
        return Base64.getEncoder().encodeToString(username.getBytes()) + ":" +
                Base64.getEncoder().encodeToString(password.getBytes());
    }
}
