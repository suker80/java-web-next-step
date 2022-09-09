package controller;

import model.User;
import util.HttpRequest;
import util.HttpResponse;

import static db.DataBase.addUser;

public class UserCreateController extends AbstractController {

    @Override
    void doPost(HttpRequest request, HttpResponse response) {

        User user = new User(
                request.getParameter("userId"),
                request.getParameter("password"),
                request.getParameter("name"),
                request.getParameter("email")

        );
        addUser(user);
        response.sendRedirect("/index.html");

    }

    @Override
    void doGet(HttpRequest request, HttpResponse response) {
    }
}
