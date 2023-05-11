package efs.task.todoapp.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static <T> String fromObjectToJson(T object) {
        if (object == null) {
            throw new IllegalArgumentException("Input cannot be null");
        }

        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error during serialization" ,e);
        }
    }

    public static <T> T fromJsonToObject(String json, Class<T> result) {
        if (json == null) {
            throw new IllegalArgumentException("Input JSON cannot be null");
        }

        try {
            return objectMapper.readValue(json, result);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Error during deserialization", e);
        }
    }
}
