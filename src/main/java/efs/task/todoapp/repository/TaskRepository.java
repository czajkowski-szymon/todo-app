package efs.task.todoapp.repository;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class TaskRepository implements Repository<UUID, TaskEntity> {
    Map<UUID, TaskEntity> tasks;

    public TaskRepository() {
        tasks = new HashMap<>();
    }

    public Map<UUID, TaskEntity> getTasks() {
        return tasks;
    }

    @Override
    public UUID save(TaskEntity taskEntity) {
        UUID uuid = UUID.randomUUID();
        taskEntity.setUuid(uuid);
        tasks.put(uuid, taskEntity);
        return uuid;
    }

    @Override
    public TaskEntity query(UUID uuid) {
        return tasks.get(uuid);
    }

    @Override
    public List<TaskEntity> query(Predicate<TaskEntity> condition) {
        return tasks.values().stream()
                .filter(condition)
                .collect(Collectors.toList());
    }

    @Override
    public TaskEntity update(UUID uuid, TaskEntity taskEntity) {
        tasks.get(uuid).update(taskEntity.getTaskDescription(), taskEntity.getDueDate());
        System.out.println(tasks);
        return tasks.get(uuid);
    }

    @Override
    public boolean delete(UUID uuid) {
        return tasks.remove(uuid) != null;
    }
}
