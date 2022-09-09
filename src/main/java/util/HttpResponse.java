package util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import webserver.RequestHandler;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

public class HttpResponse {
    private static final Logger log = LoggerFactory.getLogger(HttpResponse.class);
    private DataOutputStream dos;


    public HttpResponse(OutputStream outPutStream) {
        dos = new DataOutputStream(outPutStream);
    }

    public void forward(String s)  {
        byte[] bytes = new byte[0];
        try {
            bytes = Files.readAllBytes(Paths.get(s));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        response200Header(bytes.length,s);
        responseBody(bytes);
    }

    public void sendRedirect(String s) {
        response302Header(s);
    }

    public void responseLogin302SuccessHeader() throws IOException {
        dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
        dos.writeBytes("Location: /index.html \r\n");
        dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");
        dos.writeBytes("\r\n");


    }

    public void response200Header(int lengthOfBodyContent, String accept) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            if (accept.endsWith(".html")) {
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            } else if (accept.endsWith(".css")) {
                dos.writeBytes("Content-type : text/css\r\n");
            }
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
    private void response302Header(String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \n");
            dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");
            dos.writeBytes("Location: " + redirectUrl + '\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
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
}
