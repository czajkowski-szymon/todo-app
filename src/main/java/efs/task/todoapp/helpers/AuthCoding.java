package efs.task.todoapp.helpers;

import java.util.Base64;

public class AuthCoding {
    public static String code(String username, String password) {
        return Base64.getEncoder().encodeToString(username.getBytes()) + ":" +
                Base64.getEncoder().encodeToString(password.getBytes());
    }
}
