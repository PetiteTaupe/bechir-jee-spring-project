package ch.hearc.jee2025.bechirjeespringproject;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class AdminUtils {

    public static final String ADMIN_KEY = "secret123";

    public static void checkAdminKey(String key) {
        if (!ADMIN_KEY.equals(key)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Invalid admin key");
        }
    }
}
