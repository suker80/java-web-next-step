package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;

import java.io.*;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Map;

import static db.DataBase.*;

public class RequestHandler extends Thread {
    public static final String WEBAPP = "./webapp";
    public static final String BASE_URL = "http://localhost:8080/";
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private HttpRequest request;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            request = new HttpRequest(in);
            if (request.getMethod().equals("GET")) {

                if (request.getPath().equals("/user/create")) {
                    User user = new User(
                            request.getParameter("userId"),
                            request.getParameter("password"),
                            request.getParameter("name"),
                            request.getParameter("email")

                    );
                    addUser(user);
                } else if (request.getPath().equals("/user/list")) {
                    Map<String, String> cookie = HttpRequestUtils.parseCookies(request.getHeader("Cookie"));

                    if (Boolean.parseBoolean(cookie.get("logined"))) {

                        ArrayList<User> users = new ArrayList<>(findAll());
                        StringBuilder sb = new StringBuilder();
                        for (User user : users) {
                            sb.append(user.toString()).append('\n');
                        }
                        DataOutputStream dos = new DataOutputStream(out);
                        response200Header(dos, sb.length());
                        responseBody(dos, String.valueOf(sb).getBytes(StandardCharsets.UTF_8));

                    } else {
                        response302Header(new DataOutputStream(out), "/index.html");
                    }


                } else {
                    DataOutputStream dos = new DataOutputStream(out);
                    byte[] body = Files.readAllBytes(new File(WEBAPP + request.getPath()).toPath());
                    response200Header(dos, body.length);
                    responseBody(dos, body);
                }


            } else if (request.getMethod().equals("POST")) {

                if (request.getPath().equals("/user/create")) {
                    User user = new User(
                            request.getParameter("userId"),
                            request.getParameter("password"),
                            request.getParameter("name"),
                            request.getParameter("email")

                    );
                    addUser(user);
                    log.info(user.toString());

                    DataOutputStream dos = new DataOutputStream(out);
                    String redirectUrl = "http://localhost:8080/index.html";
                    response302Header(dos, redirectUrl);
                } else if (request.getPath().equals("/user/login")) {
                    String userId = request.getParameter("userId");
                    String password = request.getParameter("password");
                    User findUser = findUserById(userId);
                    DataOutputStream dos = new DataOutputStream(out);
                    if (findUser.getPassword().equals(password)) {
                        responseLogin302SuccessHeader(dos);

                    } else {

                        dos.writeBytes("Set-Cookie: logined=false\n");
                        dos.writeBytes("access-control-expose-headers: Set-Cookie\r\n");

                        response302Header(dos, BASE_URL + "/login.html\r\n");
                    }
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void responseLogin302SuccessHeader(DataOutputStream dos) throws IOException {
        dos.writeBytes("HTTP/1.1 302 Redirect \r\n");
        dos.writeBytes("Set-Cookie: logined=true \r\n");
        dos.writeBytes("Location: /index.html \r\n");
        dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");

        dos.writeBytes("\r\n");


    }

    private void response200Header(DataOutputStream dos, int lengthOfBodyContent) {
        try {
            dos.writeBytes("HTTP/1.1 200 OK \r\n");
            if (request.getHeader("Accept").equals("text/html")) {
                dos.writeBytes("Content-Type: text/html;charset=utf-8\r\n");
            } else if (request.getHeader("Accept").equals("text/css")) {
                dos.writeBytes("Content-type : text/css");
            }
            dos.writeBytes("Content-Length: " + lengthOfBodyContent + "\r\n");
//            dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");
            dos.writeBytes("\r\n");
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    private void response302Header(DataOutputStream dos, String redirectUrl) {
        try {
            dos.writeBytes("HTTP/1.1 302 Found \n");
            dos.writeBytes("Accept: text/css, */*;q=0.1\r\n");
            dos.writeBytes("Location: " + redirectUrl + '\n');
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void responseBody(DataOutputStream dos, byte[] body) {
        try {
            dos.write(body, 0, body.length);
            dos.flush();
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
