package cookie;

import util.HttpRequestUtils;

import java.util.Map;

public class HttpCookie {

    private final Map<String, String> cookie;

    public HttpCookie(String cookie) {
        this.cookie = HttpRequestUtils.parseCookies(cookie);
    }

    public String getAttributes(String name) {
        return cookie.get(name);
    }

    public void setAttributes(String key, String value) {
        cookie.put(key, value);
    }

}
