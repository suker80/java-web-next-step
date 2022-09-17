package cookie;

import java.util.HashMap;
import java.util.Map;

public class HttpCookie {

    private Map<String, String> cookie;

    public HttpCookie(Map<String, String> cookie) {
        this.cookie = cookie;
    }

    public String getAttributes(String name) {
        return cookie.get(name);
    }

    public void setAttributes(String key, String value) {
        cookie.put(key, value);
    }

}
