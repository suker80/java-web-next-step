package session;

import java.util.HashMap;
import java.util.Map;

public class HttpSession {
    private String id;
    private Map<String, Object> attribute;


    public void setAttribute(String key ,Object value) {
        attribute.put(key, value);
    }

    public Object getAttribute(String key) {
        return attribute.get(key);
    }

    public void removeAttribute(String key) {
        attribute.remove(key);
    }

    public HttpSession(String id) {
        this.id = id;
        attribute = new HashMap<>();
    }

    public void invalidate() {
        attribute.clear();
    }

    public String getId() {
        return id;
    }

}
