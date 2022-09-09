package webserver;

import model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import static db.DataBase.*;

public class RequestHandler extends Thread {
    public static final String WEBAPP = "./webapp/";
    public static final String BASE_URL = "http://localhost:8080/";
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private HttpRequest request;
    private HttpResponse response;

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            request = new HttpRequest(in);
            response = new HttpResponse(out);
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

                        response.response200Header(sb.length(), request.getHeader("Accept"));
                        response.responseBody(String.valueOf(sb).getBytes(StandardCharsets.UTF_8));

                    } else {
                        response.sendRedirect("/index.html");
                    }


                } else {
                    String pathname = WEBAPP + request.getPath();
                    response.forward(pathname);
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
                    response.sendRedirect("http://localhost:8080/index.html");
                } else if (request.getPath().equals("/user/login")) {
                    String userId = request.getParameter("userId");
                    String password = request.getParameter("password");
                    User findUser = findUserById(userId);
                    if (findUser.getPassword().equals(password)) {
                        response.responseLogin302SuccessHeader();

                    } else {
                        DataOutputStream dos = new DataOutputStream(out);
                        dos.writeBytes("Set-Cookie: logined=false\n");
                        dos.writeBytes("access-control-expose-headers: Set-Cookie\r\n");
                        response.sendRedirect(BASE_URL + "/login.html\r\n");
                    }
                }
            }

        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
