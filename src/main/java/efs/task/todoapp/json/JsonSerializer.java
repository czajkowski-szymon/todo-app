package efs.task.todoapp.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import efs.task.todoapp.excpetion.BadRequestException;

public class JsonSerializer {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private static String json;

    public static <T> String fromObjectToJson(T object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Brak wymaganej tresci");
        }
    }

    public static <T> T fromJsonToObject(String json, Class<T> result) {
        try {
            return objectMapper.readValue(json, result);
        } catch (JsonProcessingException e) {
            throw new BadRequestException("Brak wymaganej tresci");
        }
    }
}
