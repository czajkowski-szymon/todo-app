package efs.task.todoapp.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import efs.task.todoapp.excpetion.BadJsonException;

public class JsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    public static <T> String fromObjectToJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BadJsonException("Brak wymaganej tresci");
        }
    }

    public static <T> T fromJsonToObject(String json, Class<T> result) {
        if (json == null || json.replaceAll("\\s", "").equals("{}")) {
            throw new BadJsonException("Brak wymaganej tresci");
        }
        try {
            return objectMapper.readValue(json, result);
        } catch (JsonProcessingException e) {
            throw new BadJsonException("Brak wymaganej tresci");
        }
    }
}
