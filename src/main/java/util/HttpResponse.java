package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class HttpResponse {
    public static final String SET_COOKIE = "Set-Cookie";
    private Map<String, String> responseHeader = new HashMap<>();
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream dos;


    public HttpResponse(OutputStream outPutStream) {
        dos = new DataOutputStream(outPutStream);
    }

    public void forward(String s) {
        byte[] bytes;
        try {
            bytes = Files.readAllBytes(Paths.get(s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        response200Header(bytes.length, s);
        writeHeader();
        responseBody(bytes);
    }

    public void sendRedirect(String s) {
        response302Header(s);
        writeHeader();
    }

    public void response200Header(int lengthOfBodyContent, String accept) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            if (accept.endsWith(".html")) {
                responseHeader.put("Content-Type", "text/html;charset=utf-8");
            } else if (accept.endsWith(".css")) {
                responseHeader.put("Content-type", "text/css");
            }
            responseHeader.put("Content-Length", String.valueOf(lengthOfBodyContent));
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void response302Header(String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found");
            responseHeader.put("Accept", "text/css, */*;q=0.1");
            responseHeader.put("Location", redirectUrl);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    public void responseBody(byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    public void writeHeader() {
        responseHeader.forEach((s, s2) -> {
            try {
                dos.writeBytes(s + ": " + s2 + "\r\n");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        try {
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public String getHeader(String key) {
        return responseHeader.get(key);
    }

    public void setCookie(String value) {
        String header = getHeader(SET_COOKIE);
        if (header == null) {
            responseHeader.put(SET_COOKIE, value);
        } else {
            responseHeader.replace(SET_COOKIE, header + "; " + value);
        }
        responseHeader.put("access-control-expose-headers", "Set-Cookie");

    }
}
