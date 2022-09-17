package session;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HttpSessions {
    private static Map<String, HttpSession> sessionMap = new HashMap<>();

    public static String newSession() {
        String id = UUID.randomUUID().toString();
        HttpSession httpSession = new HttpSession(id);
        sessionMap.put(id, httpSession);
        return id;
    }

    public static HttpSession getSession(String sessionId) {
        return sessionMap.get(sessionId);
    }

}
