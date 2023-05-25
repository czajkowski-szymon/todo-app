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
        users.put(userEntity.getAuth(), userEntity);
        return userEntity.getAuth();
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
}
