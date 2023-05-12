package efs.task.todoapp.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import efs.task.todoapp.excpetion.BadJSONException;

public class JsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static <T> String fromObjectToJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BadJSONException("Brak wymaganej tresci");
        }
    }

    public static <T> T fromJsonToObject(String json, Class<T> result) {
        try {
            return objectMapper.readValue(json, result);
        } catch (JsonProcessingException e) {
            throw new BadJSONException("Brak wymaganej tresci");
        }
    }
}
