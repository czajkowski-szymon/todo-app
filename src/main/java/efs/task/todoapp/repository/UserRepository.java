package efs.task.todoapp.repository;

import efs.task.todoapp.helpers.AuthCoding;
import efs.task.todoapp.helpers.Responses;

import java.util.*;
import java.util.function.Predicate;

public class UserRepository implements Repository<String, UserEntity> {
    private Map<String, UserEntity> users;

    public UserRepository() {
        users = new HashMap<>();
    }

    @Override
    public String save(UserEntity userEntity) {
        String auth = AuthCoding.code(userEntity.getUsername(), userEntity.getPassword());
        if (users.containsKey(auth)) {
            return Responses.USER_EXISTS;
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
}
