package controller;

import model.User;
import util.HttpRequest;
import util.HttpResponse;

import java.io.DataOutputStream;
import java.io.IOException;

import static db.DataBase.findUserById;
import static webserver.RequestHandler.BASE_URL;

public class LoginController extends AbstractController {
    @Override
    void doPost(HttpRequest request, HttpResponse response) {
        String userId = request.getParameter("userId");
        String password = request.getParameter("password");
        User findUser = findUserById(userId);
        if (findUser.getPassword().equals(password)) {
            try {
                response.responseLogin302SuccessHeader();
                response.writeHeader();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        } else {
            response.sendRedirect(BASE_URL + "/login.html\r\n");
        }

    }

    @Override
    void doGet(HttpRequest request, HttpResponse response) {

    }
}
