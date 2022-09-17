package webserver;

import controller.Controller;
import controller.ListUserController;
import controller.LoginController;
import controller.UserCreateController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import session.HttpSessions;
import util.HttpRequest;
import util.HttpResponse;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;

public class RequestHandler extends Thread {
    public static final String WEBAPP = "./webapp/";
    public static final String BASE_URL = "http://localhost:8080/";
    private static final Logger log = LoggerFactory.getLogger(RequestHandler.class);

    private final Socket connection;
    private Map<String, Controller> controllerMap = new HashMap<>();
    private static final String jsessionid = "JSESSIONID";

    public RequestHandler(Socket connectionSocket) {
        this.connection = connectionSocket;
        controllerMap.put("/user/create", new UserCreateController());
        controllerMap.put("/user/login", new LoginController());
        controllerMap.put("/user/list.html", new ListUserController());

    }

    public void run() {
        log.debug("New Client Connect! Connected IP : {}, Port : {}", connection.getInetAddress(),
                connection.getPort());

        try (InputStream in = connection.getInputStream(); OutputStream out = connection.getOutputStream()) {

            HttpRequest request = new HttpRequest(in);
            log.debug(request.getPath());

            HttpResponse response = new HttpResponse(out);
            Controller controller = controllerMap.get(request.getPath());


            if (request.getCookie().getAttributes(jsessionid) == null) {
                String sessionId = HttpSessions.newSession();
                response.setCookie(jsessionid + "=" + sessionId);
            }
            if (controller == null) {
                response.forward(WEBAPP + request.getPath());
            } else {
                controller.service(request, response);
            }
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }
}
