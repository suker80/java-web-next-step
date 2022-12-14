package util;

import cookie.HttpCookie;
import session.HttpSession;
import session.HttpSessions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import static util.HttpRequestUtils.parseQueryString;

public class HttpRequest {
    private String method;
    private String path;
    private Map<String, String> queryString = new HashMap<>();
    private final Map<String, String> header = new HashMap<>();
    private HttpCookie cookie;

    public HttpRequest(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        String line = bufferedReader.readLine();
        if (line == null) {
            return;
        }

        String[] http = line.split(" ");
        method = http[0];
        path = http[1];
        int index = path.indexOf("?");
        if (index != -1) {
            String param = path.substring(index + 1);
            path = path.substring(0, index);
            queryString = parseQueryString(param);
        }
        readHeader(bufferedReader, line);

        if (getMethod().equals("POST")) {
            String body = IOUtils.readData(bufferedReader, Integer.parseInt(header.get("Content-Length")));
            queryString = parseQueryString(body);
        }
        cookie = new HttpCookie(getHeader("Cookie"));

    }


    public String getParameter(String param) {
        return this.queryString.get(param);
    }

    public String getHeader(String header) {
        return this.header.get(header);
    }

    public String getPath() {
        return path;
    }

    public String getMethod() {
        return method;
    }

    public HttpCookie getCookie() {
        return cookie;
    }

    private void readHeader(BufferedReader bufferedReader, String line) throws IOException {

        while (!line.isEmpty()) {
            line = bufferedReader.readLine();
            if (line.isEmpty()) {
                break;
            }
            String[] split = line.split(":");
            header.put(split[0].trim(), split[1].trim());


        }
    }

    public HttpSession getSession() {
        return HttpSessions.getSession(getCookie().getAttributes("JSESSIONID"));

    }
}
