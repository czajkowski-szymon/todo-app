package efs.task.todoapp.repository;

import java.util.*;
import java.util.function.Predicate;

public class TaskRepository implements Repository<UUID, TaskEntity> {
    Map<UUID, TaskEntity> tasks;

    public TaskRepository() {
        tasks = new HashMap<>();
    }

    @Override
    public UUID save(TaskEntity taskEntity) {
        UUID id = UUID.randomUUID();
        return id;
    }

    @Override
    public TaskEntity query(UUID uuid) {
        return null;
    }

    @Override
    public List<TaskEntity> query(Predicate<TaskEntity> condition) {
        return null;
    }

    @Override
    public TaskEntity update(UUID uuid, TaskEntity taskEntity) {
        return null;
    }

    @Override
    public boolean delete(UUID uuid) {
        return false;
    }
}
