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

    @Override
    public String save(UserEntity userEntity) {
        AuthEntity auth = new AuthEntity(userEntity.getUsername(), userEntity.getPassword());
        if (users.containsKey(auth.getId())) {
            throw new UserAlreadyAddedException("Uzytkownik od podanej nazwie juz istnieje");
        }
        users.put(auth.getId(), userEntity);
        return JsonSerializer.fromObjectToJson(auth);
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
