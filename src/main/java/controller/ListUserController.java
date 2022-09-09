package controller;

import model.User;
import util.HttpRequest;
import util.HttpRequestUtils;
import util.HttpResponse;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Map;

import static db.DataBase.findAll;

public class ListUserController extends AbstractController {
    @Override
    void doPost(HttpRequest request, HttpResponse response) {

    }

    @Override
    void doGet(HttpRequest request, HttpResponse response) {

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

    }
}
