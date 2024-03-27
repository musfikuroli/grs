package com.grs.api.config.security;

import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

/**
 * Created by Acer on 18-Dec-17.
 */
@Service
public class OISFPasswordService {

    public static String hashPassword(String passwordPlaintext) {
        String salt = BCrypt.gensalt();
        String hashedPassword = BCrypt.hashpw(passwordPlaintext, salt);
        StringBuilder hashedPasswordBuilder = new StringBuilder(hashedPassword);
        if (hashedPassword.startsWith("$2a$")) {
            hashedPasswordBuilder.setCharAt(2, 'y');
        }
        return hashedPasswordBuilder.toString();
    }

    public static boolean checkPassword(String passwordPlaintext, String storedHashedPassword) {
        boolean isPasswordVerified = false;

        if (null == storedHashedPassword || !storedHashedPassword.startsWith("$2y$")) {
            return false;
        }
        StringBuilder hashedPasswordBuilder = new StringBuilder(storedHashedPassword);
        if (storedHashedPassword.startsWith("$2y$")) {
            hashedPasswordBuilder.setCharAt(2, 'a');
        }
        isPasswordVerified = BCrypt.checkpw(passwordPlaintext, hashedPasswordBuilder.toString());
        return isPasswordVerified;
    }

}
