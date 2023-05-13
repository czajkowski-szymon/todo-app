package efs.task.todoapp.repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskRepository implements Repository<UUID, TaskEntity> {
    List<TaskEntity> tasks;

    public TaskRepository() {
        tasks = new ArrayList<>();
    }

    @Override
    public UUID save(TaskEntity taskEntity) {
        UUID uuid = UUID.randomUUID();
        taskEntity.setUuid(uuid);
        tasks.add(taskEntity);
        return uuid;
    }

    @Override
    public TaskEntity query(UUID uuid) {
        return tasks.stream()
                .filter(task -> task.getUuid().equals(uuid))
                .findFirst().orElseThrow();
    }

    @Override
    public List<TaskEntity> query(Predicate<TaskEntity> condition) {
        return tasks.stream()
                .filter(condition)
                .collect(Collectors.toList());
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
